Action sequencing
=

How a client's actions reach the server, in what order they are applied, and how
every client agrees on the result afterwards.

`ROADMAP.md` schedules the work and names conflict-safe text editing as the
headline risk. This document is the design behind that entry: what to build, in
what order, and why each step has to come before the next.

Sizes are the same scale as the roadmap: **S** = a day or two, **M** = about a
week, **L** = multiple weeks.

The problem
==

Nothing in the system carries a version. Not `meetings`, not the events, not the
actions. An action that says *replace 0 characters at position 7 with "a"* has
no way to express which state position 7 refers to, and the server has no way to
check. `common/domain/TextUpdate` validates only that the splice fits inside the
current string, so a stale offset that happens to land in range corrupts
silently and one that does not throws. `sequenceId` has the same flaw one level
up: the client means *the index I last saw*, the server reads *the index right
now*.

Four separate problems are tangled together underneath that.

- **The server does not order a single user's actions.** `clientInboundChannel`
  is an `ExecutorSubscribableChannel` backed by an executor, so two SEND frames
  from the same session can be handled on two threads at once. That executor
  was never configured, and the fallback picked `webSocketTaskScheduler` — the
  two threads that also send the broker heartbeats and close expired sessions —
  so every action was already competing with the heartbeats for them.
  `@Transactional` gives isolation, not ordering: the read-modify-write of
  `sequence_id` across sibling rows in `TopicService.move` can interleave with
  another action on the same meeting. Nothing on the client prevented this:
  `common/ActionWebSocketClient` was a pass-through that added a `client-id`
  header and had no queue or throttle at all.

  **Solved.** `config/SessionOrder` now serialises a session's frames and
  `meeting/MeetingLock` serialises a meeting's, both on the server. A frame
  whose turn never comes is refused with a retryable rejection rather than run
  out of order.
- **Clients cannot tell that they missed something.** Events carry
  `{target, id, mutation, origin}` and nothing else. Every *out of sync?* TODO
  in the meeting page is this gap; today the answer is `console.error` and carry
  on with a view that has quietly diverged.
- **Ordering is stored densely.** `topics.sequence_id` and `blocks.sequence_id`
  are contiguous integers, so inserting or moving one row rewrites every row
  between the old and the new position, and publishes an event for each. Two
  concurrent moves scramble the list.
- **Remote text edits corrupt each other rather than being ignored.**

  **Changed.** The meeting page now applies name, description and content
  edits from other people, and the editors keep the caret across them. So the
  divergence this section used to describe is gone, and what is left is the
  real conflict: two edits composed against the same base, spliced in the order
  they arrive. That is step 6.

Where versions belong
==

One number cannot do both of the jobs a version is asked to do, and the two jobs
want different granularities.

| Concern | Granularity | Purpose |
| --- | --- | --- |
| Event stream position | per meeting | ordering, gap detection, resync after reconnect, acknowledgement payload |
| Text conflict token | per text block | the base a transformation rebases against |
| Structure | none | fractional ranks make moves, creates and deletes commutative |

**Stream position has to be coarse.** A per-entity version only detects gaps for
entities you already know exist, and create and delete are exactly the events
that change which entities exist. If the CREATE for a topic is dropped, no
version on that topic helps, because you have never heard of it. A contiguous
counter on the stream makes the gap visible whatever it was about. This is a
position in a channel, not a version of the meeting as a lump of state — the
meeting is already the subscription unit, so it is the natural thing to number.

The hot-row objection is real but bounded by the product: the writers on one
meeting are the people in one room, tens rather than thousands.

**The conflict token has to be fine.** Transformation only ever happens between
operations on the same string, so a splice in one block can never invalidate a
splice in another. Using the stream position as the conflict token would
manufacture conflicts between blocks that cannot interfere, and resolving those
means inspecting the intervening operations to see whether they touched your
block — which is the per-block version, reached the long way round.

**Structure needs no token at all**, once ranks are fractional. Not because a
version there would be too fine-grained, but because those operations stop
conflicting. Versions are for operations that cannot be made commutative; text
is the only one here that qualifies.

The steps
==

Each step is independently shippable and leaves the system better than it found
it. The order is a dependency order, not a priority order.

Step 1 — Survive a reconnect (S)
--

`Session.update` runs on a timer before the access token expires. It calls
`disconnect()` and builds a new `WebSocketClient`, whose subscription map starts
empty. `MeetingWebSocketClient.connect` only ever runs in the page's `onMount`,
so after every token refresh an open meeting receives no events at all until it
is remounted.

Keep one `WebSocketClient` for the lifetime of the session and give it a way to
reconnect with a new token, so its subscription map survives the refresh. While
in there, `WebSocketClient.onDisconnect` reads
`registration.subscription == undefined;` — a comparison that does nothing where
an assignment was meant.

Keeping a connection alive across a refresh exposes something the old churn
was hiding. A STOMP session is authenticated once, in
`WebSocketChannelInterceptor`, and holds that authentication for the rest of
its life. Tearing the connection down every half hour used to force a
re-authentication by accident; once it no longer does, a connection outlives
the credential it was opened with indefinitely. So the backend closes it: the
token already says when it expires, and the session is scheduled to close at
that instant. The decoder rejects a token that is expired *before* CONNECT, so
what this covers is the token expiring *during* a connection.

The client half of that is the same header work: a connection the server closes
is reconnected by stompjs, and it must present the current token rather than
the one it was built with.

The session is deliberately *not* extended when the client refreshes its token,
even though that would avoid the reconnect. The authentication is derived once,
at CONNECT, and moving the deadline does not re-derive it — so extending would
let a session keep the authorities it was opened with for as long as its
refresh token lives, which `SessionInfo.ACTIVE_DURATION` puts at seven days.
Someone whose role was downgraded, or who was removed from the organisation,
would keep their old rights for a week. Closing bounds that at one access token
instead, because every reconnect rebuilds the authentication from a freshly
decoded token. It also keeps CONNECT as the only way a session is
authenticated, rather than adding an endpoint that takes a token and mutates
session state.

The cost is that each client goes quiet for a reconnect once per token
lifetime, and events published in that window are lost. That is not particular
to expiry — a dropped network, a redeploy or a closed laptop lid do the same
thing — so it is step 4's resync that answers it, for every cause at once,
rather than anything built here.

*Why first:* nothing below can be observed or trusted while a background timer
silently severs the event stream. It also makes the throttle's timeout stop
firing on every action after a refresh.

*Done when:* a meeting left open across a token refresh still receives another
participant's edits, and a connection whose token expires is closed by the
server and reconnected by the client. The events missed during that reconnect
are still lost until step 4.

Step 2 — Serialise a meeting's actions on the server (S)
--

Take a lock keyed by meeting id around the mutating path, or hand each meeting a
single-threaded executor. Either way one action per meeting is applied at a
time, in a defined order.

`meeting/MeetingLock` is the lock: one `ReentrantLock` per meeting id, held in a
map that is reference-counted so an entry is dropped once nothing holds or waits
for it, with the wait bounded by `meeting.lock.timeout`. Every mutating service
method resolves its meeting — from the action for a create, by walking up from
the element otherwise — and hands its body to `call` or `run`.

The lock opens the transaction rather than the service method declaring one.
This is the surprising part of the code and the reason `@Transactional` is
absent from methods that plainly write: the lock has to outlast the commit, and
`@Transactional` commits *after* the annotated method returns, so a lock taken
inside one is released while its transaction is still open and the next action
reads state the previous one has not written. Nothing but ids crosses into the
locked call, so every action fetches its own state and computes against what the
previous one left.

The meeting is deliberately coarser than the data needs. The scopes that
actually conflict are nested rather than parallel: a topic create, move or
delete rewrites `findAllByMeetingId`, a block one rewrites `findAllByTopicId`,
and a text edit touches a single row. Locking each of those separately means a
three-level hierarchy with ordered acquisition — a topic delete rewrites the
meeting's topic list *and* cascades into that topic's blocks — bought for
parallelism this product does not need, since the writers on one meeting are
the people in one room.

Two things make the coarse lock the better trade. Step 4's stream position is
per meeting, so every action has to take a number from that one counter in
commit order anyway; finer locks would move the queue rather than remove it,
and would let the numbering diverge from the order the data actually changed.
And the one high-frequency action, text, is better taken *out* of the lock than
split from it: step 6 gives `text_blocks` a version, and a compare-and-set on
that version is optimistic concurrency needing no lock at all. Fine granularity
arrives there, as a consequence of the version, rather than as locking
machinery here.

*Why here:* it is the only step that makes correctness independent of how
clients behave. Everything below assumes the server applies one action at a time
per meeting, and step 4's counter needs a serialisation point to be gapless.

*Done when:* two actions on the same meeting arriving simultaneously produce the
same result as the two arriving in sequence, with no client-side throttle in
play.

Step 3 — Make the order the server applies visible (M)
--

Step 2 orders the writes. This step makes that order the one clients actually
observe — two things it deliberately left behind.

**Publish after the commit, not inside it.** Every publisher handed its event to
`SimpMessagingTemplate` from inside the transaction, so a broadcast could reach
a client before the write it described had landed.
`common/messaging/TransactionalPublisher` now owns that send: with a transaction
in progress it registers an `afterCommit` synchronisation, and without one it
sends straight away. Because `MeetingLock` opens the transaction inside the lock
and holds the lock across the commit, an action's events go out after its own
commit and before the next action on that meeting opens its transaction: event
order matches apply order rather than merely resembling it. An action that rolls
back now broadcasts nothing at all. Step 4 cannot do without this — a `revision`
that has not committed cannot be sent out on an event claiming it.

Only the send is deferred, not the whole publish. Publishing still happens
inside `MeetingLock.call`. The alternative — handing the events back out of
`MeetingLock.call` for the caller to publish afterwards — moves the work outside
the lock, where two actions race to broadcast and lose the order the lock just
imposed.

**Order a single session's actions.** The lock gives mutual exclusion, not
order. Two SEND frames from one client are handed to the inbound pool before
either reaches the lock, so they can be applied in the order the threads win
rather than the order they were sent; a fair lock does not help, because the
reordering happens upstream of acquisition. `preSend` on `clientInboundChannel`
runs on the session's own receive thread, in arrival order, which makes it the
one place a session's actions can be ordered honestly.

`config/SessionOrder` is that gate: one `Semaphore(1)` per session id, taken in
`preSend` by `SessionOrderInterceptor` and handed back once the frame is done
with. Holding the receive thread stops the second frame being dispatched at all,
so the order falls out of one thread acquiring in arrival order rather than
being reconstructed from stamped positions. Only `/app` destinations wait, so a
slow action does not delay the CONNECT, SUBSCRIBE and heartbeat frames sharing
its session.

Done with means one of two things, and both have to hand the turn back. A frame
that reached the handler comes back through `afterMessageHandled`, filtered to
`SimpAnnotationMethodMessageHandler` because every subscriber of the inbound
channel is given the message on a task of its own. A frame that never got there
— refused by Spring Security after it took its turn, say, since that interceptor
runs after this one — comes back through `afterSendCompletion` with `sent`
false, which `ChannelInterceptorChain.applyPreSend` triggers on every
interceptor that already ran. Miss that second path and the permit is lost and
the session stalls for good.

The wait is therefore **unbounded, and the gate never refuses a frame**. An
earlier design bounded it and refused on timeout, which put the interceptor in
the business of answering a client. That is a job it cannot hold honestly:
everything that can send to a connection is built from the channels, and the
channels are built by asking the configurers for their interceptors, so the
dependency is a bean cycle however it is dressed up. Slowness has an owner
already — `MeetingLock` times out into `BusyEntityException` and the advice
refuses it as retryable, from a handler, with the client's `change-id` attached.
The gate's only job is order.

The cruder answer is a single-threaded inbound channel, which does give total
order but serialises every meeting, and every CONNECT, SUBSCRIBE and heartbeat
with them — a slow action then stalls a meeting page loading its snapshot, and
one slower than `heartbeatIncoming` drops sessions across the board.

Waiting in `preSend` blocks a Tomcat `http-nio` worker rather than an inbound
thread, which is the better of the two: Tomcat's pool is an order of magnitude
larger, and a session that backs up far enough to be dropped is self-limiting,
where blocked inbound threads degrade every session on the instance. The cost is
that the container stops reading that session's socket while it waits, so an
action slower than `heartbeatIncoming` risks the broker treating the session as
dead. Both of those only bite once the lock is contended for seconds, which is
already a broken state.

Giving the inbound channel a pool of its own — `ws-inbound-`, sized as Spring
would have — is what makes that choice a choice at all, rather than one of two
heartbeat threads against a Tomcat worker.

Not in scope here: the lock orders the actions one JVM sees and nothing more, so
a second instance serving the same meeting would undo the guarantee with no
symptom until rows started interleaving. The serialisation point that survives
more than one instance is step 4's `revision`, incremented in the same
transaction as the mutation — a row every action already writes, taking the row
lock as a consequence of writing rather than as a mechanism reached for. Worth
answering there rather than hardening the in-JVM lock here.

*Why here:* step 2's guarantee is otherwise invisible from outside. Everything
below reasons about the order clients see, and step 4 numbers it.

*Done when:* an event never arrives before the write it describes, and two
actions sent by one client are applied in the order they were sent.

Step 4 — Stream position and explicit acknowledgement (M)
--

Add `meetings.revision BIGINT NOT NULL DEFAULT 0`, incremented in the same
transaction as any mutation to the meeting or anything beneath it. Every
published event carries it, and so does the initial load from
`/app/meetings/{id}`, so the snapshot and the stream are comparable.

Carrying it on an event is now one field on `event/dto/EventDto`, which is
why the outbound envelope was built first rather than adding the same field to
four event types and four frontend type files.

**One number per change, not per event.** A move under dense sequence ids
publishes an event per shifted sibling, so numbering events would spend five
revisions on one drag and make the counter a position in a channel rather than
a version of the meeting. Every event of one change therefore carries the
revision that change committed at, consecutive changes differ by one, and a
jump means exactly one thing. Step 5 makes a move a single event, at which
point the two readings converge anyway.

**`MeetingLock` bumps it.** The lock already has the meeting id, already owns
the transaction and already runs once per change, which makes it the one place
a new operation cannot forget the bump — the same argument that puts the
acknowledgement in an interceptor rather than in every handler. A `MeetingLock`
that only locks is still available: `run` and `call` bump because every caller
today is a change, and the plain path underneath them is one private method, so
a non-bumping pair is two lines the day a read path wants one.

**The bump belongs to the meeting, not to the row.** `MeetingInfo.bumpRevision`
decides what the next number is and `MeetingDao.update` carries it to storage
with the name and the description, like every other field of a meeting. A
`bumpRevision` on the DAO was the first shape and put a rule in the row; a
`MeetingRevision` component around it was the second, and was a second gateway
over `MeetingRepository` that skipped the DAO ↔ BDO conversion every other
write goes through.

What is left is the shape every other change already has: read the
`MeetingInfo`, change it, write it back. `MeetingLock.bump` does exactly that
through `find` and `update`, and `MeetingStorageGateway` gains no method for
it — a gateway that grew a `bumpRevision` would be answering a question about
revisions rather than storing meetings.

*Landed.* `meetings.revision` is in `V1__init.sql`, and `MeetingLock` hands the
action a `MeetingScope` — the meeting id and the revision the change is
committing at — which travels down to `EventPublisher` as one parameter where
the meeting id used to travel alone. Re-reading the number at publish time was
the alternative and does not work: `REMOVE_MEETING` deletes the row it came
from. `EventDto` and `MeetingDetailsDto` both carry it, and `DetailsService.get`
became one `REPEATABLE READ` transaction, since a snapshot assembled from N+1
separate reads can report a revision the payload has already passed.

`MeetingScope` is the scope a change runs in, which is what every service
method needed both halves of anyway; `Origin` and `ChangeId` are the candidates
to join it. It is handed down, not bound into a publisher the action calls —
see below. The pair that hands it out stays named `run` and `call` rather than
one overloaded `change`, because two overloads taking an implicitly typed
lambda are ambiguous whatever their return types.

**Considered and rejected: the lock publishes, not the services.** The scope
travels as a parameter through ten service methods and twelve publish calls,
and handing the action a publisher already bound to it —
`change(meetingId, Consumer<Events>)` — would take it off every service
signature. It was cheap to build: each locked method's return value is
discarded at its only call site, so `<T> T call` is generic in a type nobody
reads and could have returned the scope instead, and nothing publishes outside
a lock, so the lock gave up nothing by owning it.

What it costs is the class. `MeetingLock` would own the lock, the transaction,
the revision and the event scope — the whole boundary a change runs in, which
is not a lock any more, and the rename is the smaller half of that. Against
that, the plumbing it removes is one parameter each new operation copies from
the method beside it, visible in the signature, where the wrong one does not
compile — and `MeetingScope` already made it one parameter rather than two. The lock stays a primitive about ordering and transactions and the
services keep their publish. Do not revisit this as a cleanup.

Returning a list of events from the action was never the alternative either. It
wants a common type over four deliberately unrelated `bdo` records, and a
*sealed* one is not available: the four live in four packages and the project
has no `module-info`, so `javac` refuses — *class Event in unnamed module
cannot extend a sealed class in a different package*. What is left is a
non-sealed marker interface, a union with no exhaustiveness, bought for
plumbing.

*Landed.* A change enters through one service. `meeting/ChangeService.apply`
takes the lock, dispatches on the sealed `ChangeDto` inside it and returns the
`MeetingScope` the change committed at; `MeetingWebSocket.submit` turns that
into an `AcknowledgedDto` and answers `/user/queue/acks` with `@SendToUser`,
mirroring the rejection the exception handler sends on the other queue. The
revision is a return value the whole way, visible in every signature, and a
change that threw never produces one.

**The entity services no longer lock for themselves.** Each wrapped its work in
`meetingLock.call(meetingId, scope -> doX(...))`, ten times over; now they take
a `MeetingScope` and the `doX` is the method. The lock is taken once, at the one
place a change enters, still before anything is read. What that gives up is a
service that cannot be called unlocked by construction; what replaces it is a
parameter only `MeetingLock` hands out. `MeetingService.delete` keeps its own
lock, because a meeting is deleted over REST and never arrives as a change.

*Built first and removed:* an `ExecutorChannelInterceptor` sending the
acknowledgement from `afterMessageHandled`. It cannot see the handler's return
value, so the revision had to reach it on a thread-local that `MeetingLock` wrote
after the commit — and it could not hold a `SimpMessagingTemplate` either, since
the channels are built by asking the configurers for their interceptors and the
template is built from the channels, so a constructor dependency fails the
context with *Requested bean is currently in creation*. Two workarounds for one
cause: an acknowledgement is not interceptor work. The argument that put it
there — that a new `@MessageMapping` could forget to acknowledge — assumes a
second handler this design does not have, since a new operation is a record and
a `@Type` entry.

There is no header to add. A change already carries `change-id`, minted by
`MeetingWebSocketClient` and resolved by `config/ChangeIdArgumentResolver`
exactly as `OriginArgumentResolver` resolves the client id, so an
acknowledgement names the change the client is already holding. The handler
takes it as a resolved argument beside `Origin`, and names it in the
`AcknowledgedDto` it returns.

**The acknowledgement means success and carries no status.** An earlier draft
gave it one, to tell "try again" from "this will never work". Failure has its
own channel now: `common/controller/WebSocketExceptionHandler` answers on
`/user/queue/rejections` with the same `change-id`, and
`RejectedDto.permanent` / `.temporary` is that distinction. The two TODOs this
step was going to close are closed. What is left for step 4 is the positive
half — which change committed, and at what revision — and that is the one new
destination this step introduces.

`MeetingWebSocketClient` subscribes to that queue itself and needs no help from
any page: it holds one change in flight, queues what is sent behind it, releases
on a matching acknowledgement and drops the queue on a refusal, because whatever
is waiting was composed against a change that never happened. Noticing that
`Session` handed it a new `WebSocketClient` turned out not to be needed:
`Session.start` reuses the client and calls `WebSocketClient.reconnect` when the
identity is the same, which replays the subscription map, and a new client means
a different user, who is leaving the meeting anyway.

*Landed.* The meeting page tracks the last revision it applied. A jump means it
missed something, and `MeetingWebSocketClient.resync` unsubscribes and
resubscribes `/app/meetings/{id}` for a fresh snapshot rather than continuing on
a diverged view. Destinations stay inside that client; the page asks for a
reload and never names one.

**One number is not enough, and this is the part that is easy to get wrong.**
Two different events can carry the revision the page is currently on, and they
need opposite treatment. A move publishes an event per shifted sibling, all at
one revision, so a second event at the current revision is the same change
continuing and has to be applied. An event still in flight for the revision a
*snapshot* reported is a change that snapshot already contains, and applying it
splices the same edit in twice. A single monotone cursor cannot tell those
apart: advance per event and the move's siblings look stale, hold the cursor
back and a skipped revision after a fresh snapshot looks like the change that
follows it. The page therefore carries the revision *and* whether it reached it
by applying events or by loading a snapshot — `streamed` next to `revision`.

The comparison also has to allow equality upward for the same reason: `<` the
current revision is stale, `+ 1` is the next change, and anything beyond that is
the gap. An implementation that tests `=== revision + 1` alone treats every drag
as a gap and resyncs on each one.

Events that arrive before the snapshot are buffered rather than dropped, and
replayed through the same comparison once `onLoad` has a baseline, where the
ones at or below the snapshot's revision fall out as already applied. Dropping
them was the alternative: the gap check does eventually catch the loss, but only
on the *next* event, so the view sits one change short for as long as the
meeting is quiet. This closes the two `TODO`s the page carried about initial
data not being loaded yet.

Divergence found while applying a mutation resyncs too. `findTopic` and
`findBlock` used to log *out of sync?* and continue; a mutation naming a topic
or block the page does not hold is the same divergence arriving by a different
route, so they now ask for the reload. The resync is idempotent for a change
that publishes several events, because clearing `revision` is what marks one as
already in flight.

The stale view stays on screen for the round trip instead of blanking to
`Loading`. Events keep arriving and are replayed onto the snapshot, so the
divergence outlives the request by nothing, and a transient gap does not throw
the note-taker back to a spinner mid-meeting.

**The counter is blind inside a change, and the fix is one event per change.**
Numbering changes rather than events detects a gap *between* changes and cannot
see a partial one: receive three of a drag's five moves and the next change
still arrives at `+ 1`, applies cleanly, and leaves two topics on a stale rank
with nothing to notice it. Permitting equality is what costs this — a duplicate
at the current revision is indistinguishable from a legitimate sibling, so it is
applied twice, which a move survives and a splice would not.

Two properties keep that latent rather than live, and both are worth knowing
because the hole opens the moment either stops holding. A change is one
transaction, so a snapshot never lands mid-change: `DetailsService.get` sees all
of a change or none of it and reports the revision to match, which is what makes
dropping events at exactly the snapshot's revision safe. And within one
connection STOMP is ordered and lossless, while a drop takes the subscription
with it — `WebSocketClient.onConnect` replays the subscription map, including
`/app/meetings/{id}`, so a reconnect re-fires the snapshot and rebaselines the
page whether or not the gap check noticed. Partial loss therefore self-heals one
round trip later.

What opens it is anything that drops or duplicates one message without dropping
the subscription: an external broker relay and its message TTLs, a relay
restart, or per-message acknowledgement actually being honoured. Note that
`WebSocketClient` subscribes with `ack: "client-individual"` and nothing ever
calls `ack()`; the simple broker appears to ignore ack mode, which is why this
has never surfaced, but that is read from behaviour rather than demonstrated,
and redelivery is exactly the duplicate the equality case waves through.

**So the invariant to hold going forward is one change, one event, one
revision.** It is not true today only because dense `sequence_id`s make a move
shift its siblings, and step 5 removes that — after it, the revision numbers a
single event and the blindness has nowhere to live. Until then, an operation
that publishes several events per change is tolerated, not endorsed: adding a
new one is the moment to ask whether it can write a single row instead. An event
index and count on `EventDto` would make a partial change detectable sooner, and
was considered and set aside — it carries a field on the wire that step 5
deletes, and it detects the duplicate without fixing it.

**The meeting id is not part of this work.** An earlier draft put it on the
action, resolved by an argument resolver next to `Origin`, to spare the walk up
the tree for a lock key. It was settled the other way instead: the id is a
`@DestinationVariable` on `/app/meetings/{id}/changes` and travels down as a
parameter — `changes.apply(origin, meetingId, change)`, and below the lock as
the `MeetingScope` — so the lock is taken before anything is read and the client
never states the same fact twice.
Do not reintroduce it as a header, and do not denormalise `meeting_id` onto
`blocks`; both have been tried and removed. The walk that remains is the
authorisation check inside `getById`.

The publishers no longer pay a walk either. `BlockPublisher` and
`TextBlockPublisher` each re-read the topic on *every* published event — so a
move paid one lookup per shifted sibling — purely to work out the destination.
The four publishers are now one `meeting/EventPublisher` taking the meeting id
as a parameter, sourced from the destination like the lock key.

*Why here:* it turns divergence from invisible into detectable, which is the
precondition for trusting anything below. It also replaces the current
acknowledgement signal, which is an echoed broadcast event that belongs to the
meeting page and does not correspond one-to-one with actions.

*Done when:* a client that misses an event reloads instead of drifting, and an
in-flight change is released by its own acknowledgement rather than by an echoed
broadcast. Both done, and the queue the release hooks into is the one step 7
merges keystrokes in rather than only holding them back.

**An acknowledgement that never arrives is not answered at all yet.** Within a
connection STOMP is ordered and lossless and every failure path answers on
`/user/queue/rejections`, so the only way to wait forever is to lose the socket
with a change outstanding — and then the tab stops sending, because
`WebSocketClient` sets `reconnectDelay: 0` and the connection does not come
back. That is deliberate. Reconnecting on its own is the worse failure: the
replayed subscription re-fires the snapshot and rebaselines the page over the
edits the sender never got out, so they disappear from the screen unannounced.
A releasing timer is not the answer either — it guesses at a fate the client
cannot know, and a change that did commit would be replayed on top of itself.
Recovery is real work and it is scheduled: *Reconnect and recover* alongside
*Sync status*, and properly once step 6's base versions make replaying an
outstanding change safe rather than hopeful. Step 8 is that design.

Step 5 — Fractional ranks instead of dense sequence ids (M)
--

*Landed.* `topics.sequence_id` and `blocks.sequence_id` are a `rank TEXT`,
backfilled by `V2__ranks.sql` from the sequence they replaced.
`common/domain/Rank` is the whole algorithm: base-62 digits in ASCII order, so
lexicographic comparison *is* the ordering, and `Rank.between(lower, upper)`
answers for every pair, with either end open. A rank may not end in the first
digit of the alphabet, because such a rank has nothing below it but its own
prefixes and those run out — that one invariant is what lets `before` always
find room. The repositories sort by `(rank, id)`, so two ranks that do collide
still have a deterministic total order.

A move writes **one row** and publishes **one event**. Creates and deletes touch
no siblings at all, and the `ArrayList<TopicEvent>` and `ArrayList<BlockEvent>`
shifting loops are gone from `TopicService` and `BlockService`.

**The server computes the rank, not the client.** The design above said the
frontend should compute one and put it on the action. It should not: two people
dropping into the same gap compute the *same* string from the same neighbours,
and while `(rank, id)` keeps the order total, nothing can ever be inserted
between those two afterwards — there is no midpoint between equal ranks. Jitter
makes that unlikely rather than impossible. Computing server-side removes the
class: `MeetingLock` serialises a meeting's changes, so the second move reads
the first one's committed rank and gets a genuinely distinct midpoint.

So a change names the sibling it follows rather than a position:
`MOVE_TOPIC {topic, afterId}`, `ADD_BLOCK {topic, blockType, afterId}`, with
`afterId` null for the front. `TopicService.rankAfter` computes between that
neighbour and its current successor, excluding the moved entity from its own
bounds. The outbound mutation carries the resulting `rank`, read off the saved
entity rather than off the action. A stale client now sends something that still
means what it said — *after Blockers* — where a stale index was simply wrong.

`TopicAction.Move` is therefore no longer a `TopicAction.Update`: it names a
neighbour rather than a value, so only the service holding the siblings can turn
it into a rank, and there is nothing for `apply` to do. `BlockAction.Update`
held only `Move` and is gone entirely.

**Considered and rejected: writing only the field that changed.** Ranks make a
move write one row, which exposes that `TopicDao.update` copies every mutable
field off the BDO, so the row a move writes carries back the name it was read
with. That loses a rename only if two writers touch one meeting at once, and
`MeetingLock` means they cannot: every change is serialised and re-reads inside
its own transaction. The DAO stays a whole-object mirror of the BDO, which is
where the logic is guarded. When the in-process lock stops holding — horizontal
scaling — the answer is optimistic locking on a version column, not partial
writes: it keeps the write whole and fails loudly instead of silently merging.

*Why here:* it is the largest structural win available and it depends on nothing
above, but it wants step 4's acknowledgement in place first so the one-event
-per-action property can actually be relied on. It closes the roadmap's
*reordering events are not broadcast* bug by removing the shift rather than by
broadcasting it.

One event per change is not a side effect of this step, it is half the point.
Step 4's revision numbers a change and so cannot see a change arriving in
pieces; collapsing a move to one event makes *one change, one event, one
revision* true, at which point the counter numbers events and changes alike and
the gap check stops having a blind spot. Treat that as the invariant this step
owes the ones above it, not as a bonus.

*Done when:* dragging a topic in a long agenda produces a single event, and two
people reordering the same list concurrently end up with the same order. Both
done — `MeetingChangeWebSocketTest.move` pins the first, and the second follows
from ranks being computed under the lock and sorted by `(rank, id)`.

Step 6 — Operation log, base revision, transformation (L)
--

This is the roadmap's Phase 3 entry, and the only place a transformation is
needed. Applying remote text events at all, the precondition, is done: the page
has a branch per text mutation, and `editor/TextEdit` holds the splice.

A first implementation of this step was built end to end, backend, frontend and
docs, and worked: two concurrent renames converged through the real path. It was
stashed rather than committed because it landed as one change of about sixty
files (`doc/LESSONS.md`, *Step 6, the first time*). What follows is the design
it arrived at, which replaces the per-block version this section used to
propose, and the order to build it in again.

**The base is the meeting revision, not a per-block version.** Step 4's
revision already rides every event and every snapshot, and the page applies
events strictly in revision order, so the last revision a tab applied names
exactly the state its positions refer to. A per-block counter would have had to
ride the snapshot and every event as well and be tracked per block by the
client, to say nothing the revision does not. The objection that the revision
would manufacture conflicts between blocks assumed a compare-and-set: an edit
that is *rebased* rather than refused is only ever moved over edits to its own
text, so nothing is refused and there is no false conflict. A log keyed by
meeting revision is also the one *Meeting history and replay* wants.

It travels as a `base` field in the body of the five text changes —
`RENAME_MEETING`, `DESCRIBE_MEETING`, `RENAME_TOPIC`, `DESCRIBE_TOPIC` and
`EDIT_TEXT_BLOCK` — beside the flattened edit: `{"type": "RENAME_TOPIC",
"topic": 32, "base": 41, "position": 4, "length": 2, "value": "new"}`. The
positions mean nothing without the revision they were counted in, so the two
are one fact and travel together; no other change carries it. A negative base
is refused by `@Valid` like any other malformed field, and a base that is not
below the change's own revision is refused by the rebase. It is a primitive
`long` and so required: Jackson 3 refuses a frame without it, or with `null`,
rather than defaulting to 0 as Jackson 2 did — checked, and it matters, because
0 is a real base, *seen nothing*, and would rebase the edit over the whole log.
It is added in the same step as the client starts sending it, so no step leaves
the app refusing text edits and none carries a nullable stand-in.

A `base-revision` header with a `BaseRevisionArgumentResolver`, like
`change-id`, was built first and dropped. The argument for it — the base is a
fact about what the tab had seen, not about the change — does not survive the
change being a splice: the positions *are* what the tab had seen. It made every
change carry a value that only text edits read, put a parameter on
`ChangeService.apply` that every other change ignores, and needed a
`BaseRevision.NONE` and a rule refusing text edits over it, where a body field is
simply validated. The reason `change-id` is a header does not carry over:
a refusal has to name the change even when the body is unreadable, but a body
that cannot be read has nothing to rebase.

**Log every event, as sent.** An `events` table —
`(meeting_id, revision, user_id, client_id, mutation)`,
`UNIQUE(meeting_id, revision)`, which is also the index the rebase query needs — written by
`event/EventService` in the change's own transaction, so the log and the broadcast
cannot disagree and a new operation is logged without anyone writing code for
it. The origin is two columns, the principal's user and the client, and there
is no `organisation_id`: unlike every other row below the organisation, an
event is only read by meeting, inside a change already authorised, so the
column would be written and never read. The `mutation` column is the `MutationDto` the
clients received — the rest of the envelope is the row — as `JSONB`. The DAO's
field is the `MutationDto` itself under `@JdbcTypeCode(SqlTypes.JSON)`, so
Hibernate's Jackson 3 format mapper writes and reads it and the DAO keeps no
`JsonMapper` of its own. `JSONB` normalises it — keys reordered,
whitespace dropped — so the stored text is not byte for byte what was sent, and
nothing needs it to be: clients parse it, and the only reader parses it back
into a `MutationDto`. That was checked for every mutation type, including those
where `type` no longer comes first and the flattened `@JsonUnwrapped` edit comes
back reversed. `JSON` was used first, to keep the bytes, and needed a
`@ColumnTransformer(write = "?::json")` cast to insert at all; PostgreSQL's own
advice is to prefer `JSONB` unless key order matters, and it does not here. A
meeting's delete publishes before it deletes, so its event is written while the row still exists
and cascades away with the rest. Considered and rejected: a text-only
`text_operations` log with a `TextField` enum, which restated the mutation
vocabulary (`TOPIC_NAME` is `RENAME_TOPIC`) and served one caller, where *Ask for
what was missed*, replay, undo and attribution all want every event. The cost is
that once anything is deployed, a change to a mutation's JSON is a migration of
history.

**The event is the outcome, stored as the mutation the clients receive.**
Services describe what a change did as a mutation —
`TopicMutation.Move(topicId, Rank)`, `TextBlockMutation.Edit(blockId, …)` —
built from the saved entity, wrap it in an `EventInfo(scope, origin, mutation)`
and hand it to `EventService.publish`, which stores it and sends what was
stored. The existing `*Event` records held `(*Info, *Action, Origin)`, the
request, which the log cannot give back: it stores a rank and never the
`afterId` that asked for it. The mutations live together in `event/bdo` so that
`Mutation` can be sealed and `MutationDto.of` is exhaustive. `EventDao` writes
the mutation as its `MutationDto` and reads it back with `MutationDto.toBdo()`, so
the gateway returns a whole `EventInfo` like every other gateway, and rebasing
reads the log as BDOs. `*Action.Delete` has no use left once the services stop
sending actions.

**Rebase in `ChangeService`, inside the lock.** `TextHistory.rebase(scope,
base, change)` reads the events after the base, keeps those that edited the same
text — matched on the mutation the change is named after, `RENAME_TOPIC` for
`RENAME_TOPIC` on the same topic — and moves the incoming edit over each in turn.
The lock is what makes that read correct, which is why text stays inside it
rather than leaving for a compare-and-set. The change record turns the rebased
splice into its action, `TopicChangeDto.Rename.toBdo(rebased)`, so the entity
services apply, store and publish the edit the server actually made, and never
see the log. That removes the `TopicChangeDto.Update` level: `Rename`,
`Describe` and `Schedule` no longer share a `toBdo()`. Rebasing in each service
instead would thread `base` through three of them, including `Schedule`, which
ignores it.

**The rebase reads a window, not the history.** `findAllSince` returns only the
events after the change's base, and the base is the revision the tab last
applied — advanced on every event it receives, stamped as the change leaves the
queue, and never far behind because a gap resyncs. So the read covers what
committed during one round trip, over the `(meeting_id, revision)` index, and
grows with how busy the meeting is rather than how long its history is. Within
that window, edits to other texts are read and discarded. A revision per field
was considered to avoid that and rejected: it is the per-block version this
design replaced, a counter every snapshot and event would have to carry. If the
discarded rows ever matter, the answer is two columns copied from the mutation
as it is written — its `type` and the id of the entity it names — indexed with
the meeting and revision, so the database filters on them; one log and one
revision stay.

**The transform is a value type.** `common/domain/Splice(position, length,
value)` replaces the raw triple in the actions, and `TextEditDto` converts to
and from it. `rebasedOnto(prior)` follows one rule: a character survives unless
either edit removed it; each insertion stays before the character it was typed
in front of; at a tie the edit applied first goes first; and when an edit
replaces a range the other inserted into, the other's insertion is kept, because
somebody typed it. Test it exhaustively — every pair of edits over a
four-character text — against that rule stated independently of the transform.
The client runs the mirror, `editor/TextEdit.rebased`, with a `first` flag: the
server puts what it applied earlier first at a tie, so a client moving a remote
edit over its own pending one must say the remote one goes first. Test that both
directions converge over every pair.

**The client.** `MeetingWebSocketClient` takes over the event stream from the
page (revision, the pre-snapshot buffer, the gap check), because the base
revision and the rebase need the stream and the send queue together. The page
only applies what it is handed. Then:

- Each change goes out with the revision it was written against.
- The next change is released only when the acknowledgement has arrived **and**
  the revision it names has been applied. They come on different subscriptions;
  sending on the acknowledgement alone can claim a base the page does not hold,
  and the server would rebase an edit over its own predecessor.
- A remote text edit is rebased over the page's own edits the server has not yet
  put in a revision (the one in flight until its echo, then the queue), and they
  over it.
- The echo of its own *text* edit is dropped, because the editor applied it
  before sending; structural echoes are still applied. The page already does
  this through `isOwnEvent`; it moves into the client with the stream.
- A snapshot drops the queue, and a refusal drops it and reloads: the page shows
  edits the server will never hold, so only a snapshot puts it back on the
  server's text.

*Build it in this order*, each step compiling, green and reviewed before the
next:

1. The `events` table, `EventDao` and `EventRepository` in `event/`, written
   by nothing yet. *Done.*
2. The mutation BDOs, used by nothing yet. Until `Splice` exists they carry a
   text edit as `position`, `length`, `value`, as the actions do. *Done.*
3. Services publish their mutation instead of an `*Event` record;
   `*MutationDto.of(*Mutation)`; the four `*Event` records and
   `*Action.Delete` go. *Done.*
4. `EventService.publish(EventInfo)` in `event/`, storing every event
   through `EventStorageGateway` and sending what was stored. A meeting's delete
   publishes before it deletes, or the event's foreign key has no row.
   `MeetingChangeWebSocketTest.Log` checks the row against the frame a
   subscriber received. *Done.*
5. `Splice` in `common/domain` with `rebasedOnto` and its exhaustive test; the
   actions and text mutations carry a `Splice` instead of three fields. No wire
   change. *Done*: the test checks all 1,089 pairs of edits over `"abcd"`
   against the merge rule written out independently, and two conditional
   boundaries in `startAfter`/`stopAfter` are equivalent mutants — the
   neighbouring branch yields the same index there.
6. `MeetingWebSocketClient` takes over the stream from the page — the
   revision, the pre-snapshot buffer, the gap check, resync and dropping its own
   text echo — with no change in behaviour. The page only applies what it is
   handed. *Done*, with the stream tested for the first time: the buffer, both
   halves of the `streamed` rule, a gap reloading once, events replayed onto the
   reloaded snapshot, and which echoes are dropped.
7. The client releases the next change only when the acknowledgement has
   arrived **and** the revision it names has been applied, and stamps `base` on
   a text change as it releases it: the revision the page holds then, not when
   the edit was typed. In the same step the five text change records gain a
   required `@PositiveOrZero long base`, read by nothing yet, so the wire
   changes once, on both sides. *Done*: the client also sends nothing before
   the first snapshot, since it has no base to stamp; `ChangeDtoTest` refuses a
   missing and a `null` base, and `MeetingWebSocketTest.negativeBase` a
   negative one.
8. `TextHistory` and rebasing in `ChangeService`; a base not below the
   change's own revision is refused. A WebSocket test drives two concurrent
   renames through the real path to a second subscriber. *Done*: `TextHistory`
   lives in `meeting/` beside `ChangeService`, the five text records share a
   sealed `TextChangeDto` and convert only through `toBdo(Splice rebased)`, and
   `MeetingChangeWebSocketTest.Rebase.concurrent` fails with the rebase
   switched off.
9. `editor/TextEdit.rebased` and its convergence test. *Done*: it is checked
   against the same independently written merge rule as `Splice`, and for every
   pair of edits over `"abcd"` a page that applied its own edit and then the
   remote one arrives at the server's text. As in `Splice`, the boundary at the
   prior's position in `stopAfter` is an equivalent mutant.
10. The client rebases a remote text edit over its own edits the server has not
    yet put in a revision, and they over it. *Done*, with the two design
    bullets no earlier step had taken: a snapshot drops the queue, and a
    refusal drops it and reloads. The own-echo drop now applies only while the
    change in flight is pending, so after a snapshot its echo is applied.
11. `PRODUCT.md` (text merging decided), `WEBSOCKETS.md` (the `base` field, and
    `client-id`/`change-id` now read), `ROADMAP.md` and `CLAUDE.md`, each with
    the step that makes them true rather than at the end. *Done*: `PRODUCT.md`
    moves text merging and the fate of a refused change's queue to *Decided
    since*; `WEBSOCKETS.md` gains the acknowledgement queue and the rebase in
    the frame's path, and drops what had gone stale — both client ids
    unconsumed, several events per change, and a bounded session gate.

The client goes first so that rebasing is never switched on while a base can be
missing or stale. Server rebasing came first in the original order, with the
client catching up at the end; that needed a nullable `base` in between, applied
unrebased, and until the client released on revision as well as on the
acknowledgement, a base read at release could be one behind — the echo that
moves the page's revision can arrive after the acknowledgement — and the server
would have rebased an edit over the tab's own previous one.

*Not covered even then:* an IME composition is sent at `compositionend`, so a
remote edit applied mid-composition lands in text the client has not described
to anyone; the log is never pruned; and a reload that discards unsent edits says
nothing, which is *Sync status*'s job.

*Done when:* two people typing into the same block at the same time converge on
the same text, and neither loses a character.

Step 7 — Compose buffered operations (S)
--

Once operations compose, the buffer merges the keystrokes typed while an action
is in flight into a single operation, so a fast typist sends one action per
round trip instead of one per character. This is the debounce the throttle was
always heading towards, and it is nearly free once step 6's compose exists.

*Done when:* typing a sentence quickly produces a handful of actions rather than
one per character.

*Done.* Step 6 built a rebase, not a compose, so composing was written here:
`editor/TextEdit.composed(first, second)` is the one edit that does what the two
do in turn, and `MeetingWebSocketClient.send` merges a text change into the last
change waiting when both edit the same text. It merges only when the second edit
touches what the first produced — a composed edit is one splice, and two edits
apart would need the unchanged text between them, which an edit does not carry.
Ordinary typing always touches: each keystroke lands at the end of the last, or
inside it. It merges only into the *last* change waiting, never past a change
queued after it, and never into the one in flight, which has been sent. A merged
`send` returns the id of the change it joined. There was no throttle to replace:
the editors send a change per input event.

Step 8 — Replay what was missed, then reconnect (M)
--

This is the roadmap's *Ask for what was missed*, *Reconnect and recover* and
*Sync status*, which it says to do together. They share one mechanism: a client
that knows the last revision it applied asks for the events after it, rather
than for the whole meeting.

**What a gap costs today.** A gap in the revisions reloads the snapshot, and a
snapshot replaces the page: it drops the queue, and the change in flight is no
longer on screen. A dropped connection is not recovered at all, and the
reconnect that a token refresh already does replays the subscription map, which
re-fires the snapshot and rebaselines the page in the same way. An
acknowledgement lost in that window leaves the queue stuck until a reload. The
log that step 6 added holds everything a client could have missed, so none of
this has to cost the page anything.

**Replay is its own destination, answered from the log.**
`/app/meetings/{id}/events/{after}` is a `@SubscribeMapping` beside the
snapshot's, authorised the same way, replying once to the subscriber with the
`EventDto`s after `after` in revision order — `EventStorageGateway.findAllSince`,
already written for the rebase. It is a destination rather than a flag on the
snapshot subscription because the two answer different questions, and the
reply is the same `EventDto` the topic carries, so a replayed event goes through
exactly the path a live one does: the revision check, the rebase over the
page's unconfirmed edits, the own-echo drop. The read takes no lock. An event
that commits during it is either in the read or broadcast afterwards, and the
revision check drops whichever copy comes second.

**The client replays a gap instead of reloading.** It keeps its revision,
marks a replay in flight, and buffers live events until the reply is in, then
applies the reply and the buffer through `#accept`, as it already does around a
snapshot. The queue and the change in flight survive, because nothing replaced
the page. The snapshot stays for the first load, and for divergence the page
finds itself — an event naming a topic it does not hold — which no replay can
repair.

**`streamed` goes.** It told a second event of the same change from a repeat at
the snapshot's revision. One change is now one event and one revision (step 5),
so an event at the current revision is always one the page already holds: *seen*
becomes `revision <= current`, and one number is enough after all. Replay makes
this matter, because a replayed event and its live copy arrive at the same
revision, and the equality rule would apply the second.

**A resend is made safe on the server, not guessed at on the client.** After a
reconnect the client cannot know whether its change in flight committed: the
acknowledgement may have been lost, or the frame may never have arrived. That
is why nothing is resent today. The server can know. Each event row records the
`change-id` that caused it, and `ChangeService`, inside the lock, looks the id
up before it applies anything: a change already in the log is acknowledged at
its logged revision and not applied again. The client then resends the change
in flight after every reconnect, and whichever happened, it is applied once.
The lookup has to come before the revision is bumped — spending a revision on a
change that is not applied would open a gap for every other client — so it
needs `MeetingLock`'s plain lock, which is private today and becomes the
non-bumping pair that section already anticipates. A `UNIQUE(meeting_id,
change_id)` backs it up, `change_id` being null for REST-created events.
Rejected: adding the change id to `EventDto` so the client finds its own echo
in the replay. It answers only the case where the change committed, and leaves
a frame that is still being handled on the old connection racing the resend.

**A reconnect replays, it does not reload.** The snapshot subscription becomes
one-shot — the client unsubscribes once the reply is in — so the subscription
map a reconnect replays no longer holds it. On reconnect `MeetingWebSocketClient`
asks for the replay after its revision, then resends the change in flight.
`WebSocketClient` stays a dumb transport; it only reports that it has connected
again. Only then does `reconnectDelay` go back on, which is the condition the
note at the end of step 4 set.

**Sync status says which of these is happening.** One place in the UI: live,
catching up (a replay or a reload in flight), or offline (reconnecting), with
the number of changes not yet acknowledged. A reload that drops unsent edits
says so there rather than in the console.

*Build it in this order*, each step green and reviewed before the next:

1. The replay destination on the server, with a WebSocket test that sends
   changes, subscribes after one of them and receives the rest in order.
   *Done*: `event/EventWebSocket` over `EventService.findAllSince`, which checks
   the meeting's organisation first; `MeetingChangeWebSocketTest.Replay` checks
   the reply against the frames a subscriber received live.
2. The client replays a gap instead of reloading, and `streamed` goes; the
   queue and the change in flight survive a gap. *Done*: a reload abandons a
   replay in flight, or live events would stay buffered behind it; a late answer
   needs no guard, since it holds only committed events and the revision check
   drops what the page already has.
3. `change_id` on `events`; a change whose id is already logged is acknowledged
   at its logged revision without being applied, checked before the bump.
   *Done*: the id reaches the event through `MeetingScope`, so no entity service
   changed; `MeetingLock.hold` is the non-bumping lock, and `ChangeService`
   calls `call` inside it only for a change not yet logged.
   `MeetingChangeWebSocketTest.Resend` sends one rename twice under one id and
   sees one event, two acknowledgements at one revision, and the name changed
   once. Four existing tests had been sending two changes under one id; they
   now use two.
4. The one-shot snapshot, replay and resend on reconnect, and `reconnectDelay`
   back on.
5. Sync status.

*Not covered:* a replay the log can no longer answer, because nothing prunes it
yet — when pruning lands, the reply has to be able to say *too far back*, and
the client falls back to the snapshot.

*Done when:* closing a laptop mid-meeting and opening it again leaves the page
current, with nothing the note-taker typed lost or applied twice.

Not doing
==

**A CRDT** — Yjs or Automerge — is the honest alternative to step 6, and it
removes the need to write a transform at all while making offline editing work.
It is not the recommendation here. The server would become a relay around an
opaque binary document, which fights a backend that stores `content TEXT` and
will want to search, export and attribute it. The Java bindings are also well
behind the JavaScript ones, which would push the authoritative copy into a
sidecar service. For one text field per block, the transform is the smaller and
far more debuggable commitment.

Revisit this if offline editing becomes a requirement, which is the case a CRDT
wins outright.

Open questions
==

- **Does the operation log stay forever?** It is needed for rebasing only back
  to the oldest version any live client holds, but the same table would serve
  undo and redo, which both editors flag as a TODO, and authorship attribution,
  which the roadmap wants in the same phase. Those want it kept.
- **What happens to an action queued behind one that failed?** Settled: they
  are dropped and the page reloads, because the page shows the refused edit and
  whatever was written after it. Rebasing them over the refusal was the
  alternative, and there is nothing to rebase over — the refused edit never
  happened on the server, and the page's text is the thing that is wrong.
- **Should a rank be computed by the client or the server?** The client knows
  the neighbours it dropped between; the server would have to be told them
  anyway. Client-computed is simpler and standard, and the `(rank, id)` sort
  makes a collision harmless — but it does mean trusting a client-supplied
  ordering key.
- **Moving a block between topics** does not exist yet: `BlockAction.Move`
  carries only a sequence id. Ranks handle it as a one-row write once it does,
  which is a good reason not to scope any version to a topic.
