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
  another action on the same meeting. The client-side throttle in
  `common/ActionWebSocketClient` is currently the only thing preventing this,
  which is far too much weight on a queue that lives in the browser.
- **Clients cannot tell that they missed something.** Events carry
  `{target, id, mutation, origin}` and nothing else. Every *out of sync?* TODO
  in the meeting page is this gap; today the answer is `console.error` and carry
  on with a view that has quietly diverged.
- **Ordering is stored densely.** `topics.sequence_id` and `blocks.sequence_id`
  are contiguous integers, so inserting or moving one row rewrites every row
  between the old and the new position, and publishes an event for each. Two
  concurrent moves scramble the list.
- **Remote text edits are never applied.** `TextBlockPublisher` broadcasts
  `TEXT_BLOCK` events to `/topic/meetings/{id}`, but the frontend's
  `MeetingMessage` union does not include `TextBlockEvent` and the page has no
  branch for that target. Two people typing in the same block do not see each
  other at all until a reload. Conflict resolution is moot until this is fixed —
  there is currently no conflict, only silent divergence.

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

Only the send is deferred, not the whole publish. `BlockPublisher` and
`TextBlockPublisher` walk up the tree to find their meeting, and that read
belongs inside the transaction that produced the event. The alternative —
handing the events back out of `MeetingLock.call` for the caller to publish
afterwards — moves those reads after the commit and, worse, outside the lock,
where two actions race to broadcast and lose the order the lock just imposed.

**Order a single session's actions.** The lock gives mutual exclusion, not
order. Two SEND frames from one client are handed to the inbound pool before
either reaches the lock, so they can be applied in the order the threads win
rather than the order they were sent; a fair lock does not help, because the
reordering happens upstream of acquisition. `preSend` on `clientInboundChannel`
runs on the session's own receive thread, in arrival order, which makes it the
one place a session's actions can be ordered honestly.

`config/SessionOrder` is that gate: one `Semaphore(1)` per session id, taken in
`preSend` by `SessionOrderInterceptor` and handed back in `afterMessageHandled`.
Holding the receive thread stops the second frame being dispatched at all, so
the order falls out of one thread acquiring in arrival order rather than being
reconstructed from stamped positions. Only `/app` destinations wait, so a slow
action does not delay the CONNECT, SUBSCRIBE and heartbeat frames sharing its
session; and the hand-back is filtered to `SimpAnnotationMethodMessageHandler`,
because every subscriber of the inbound channel is given the message on a task
of its own. A permit that is never handed back — a frame rejected by Spring
Security after it took its turn, say, since that interceptor runs after this one
— would stall the session for good, so the wait is bounded by
`websocket.order.timeout` and gives up by taking the lost permit over rather
than by failing the action.

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

Actions carry an `action-id` native header alongside `client-id`, resolved the
same way `OriginArgumentResolver` resolves the client id. An
`ExecutorChannelInterceptor` registered in `WebSocketConfig` acknowledges it:
`afterMessageHandled` fires once per handled message, filtered to
`SimpAnnotationMethodMessageHandler`, and sends `{actionId, revision, status}`
to the sending session on `/user/queue/acks`. A new `@MessageMapping` cannot
forget to acknowledge, for the same reason it cannot forget its `Origin`. This
also closes both TODOs in `WebSocketExceptionHandler` about letting the client
identify which request failed.

`ActionWebSocketClient` then subscribes to that queue itself and needs no help
from any page: it releases the in-flight action on a matching acknowledgement
and drops the queue on a failure. Because the queue destination belongs to the
session rather than to a meeting, it can also notice that `Session` handed it a
new `WebSocketClient` and resubscribe on its own.

The meeting page tracks the last revision it applied. A jump means it missed
something, and it resubscribes to `/app/meetings/{id}` for a fresh snapshot
instead of continuing on a diverged view.

Two things step 2 left behind belong in the same header work. **The meeting id
should travel with the action**, resolved by an argument resolver next to
`Origin` exactly as `client-id` is. Today every action that names a topic or a
block walks up the tree to find its meeting — two lookups for a topic action,
three for a block or text one, before the action has done anything — purely to
pick a lock key. The client always knows the meeting; it is the page it is on.
The server then has to check the element belongs to the meeting it was told,
which is a field comparison on an entity the action loads anyway, not another
query. `BlockPublisher` and `TextBlockPublisher` pay the same walk on *every*
published event to work out the destination, so a move publishes one per shifted
sibling. (Denormalising `meeting_id` onto `blocks` is not the answer — the
column existed once and was removed.)

**And the acknowledgement should carry why an action failed.** A lock that times
out throws, and `WebSocketExceptionHandler` turns that into the same opaque
`Error` as a missing entity, so a client cannot tell "try again" from "this will
never work". The `status` in the acknowledgement is where that distinction
belongs.

*Why here:* it turns divergence from invisible into detectable, which is the
precondition for trusting anything below. It also replaces the current
acknowledgement signal, which is an echoed broadcast event that belongs to the
meeting page and does not correspond one-to-one with actions.

*Done when:* a client that misses an event reloads instead of drifting, and the
meeting page contains no throttle code at all.

Step 5 — Fractional ranks instead of dense sequence ids (M)
--

Replace `topics.sequence_id` and `blocks.sequence_id` with a `rank TEXT` that
sorts lexicographically, backfilled from the current sequence. Inserting between
two siblings picks a value between their ranks; a base-62 midpoint utility with
exhaustive tests is the whole algorithm.

A move then writes **one row** and publishes **one event**. Creates and deletes
touch no siblings at all, which removes the `ArrayList<TopicEvent>` and
`ArrayList<BlockEvent>` shifting loops from `TopicService` and `BlockService`
entirely. Two concurrent moves converge on their own; sort by `(rank, id)` so a
rank collision still has a deterministic total order rather than needing a
unique constraint that would reject one of two valid drags.

On the frontend, `ReorderHandler.handleDrop` computes a rank between the
neighbours either side of the gap rather than an index, and the move action
carries that rank.

Ranks make a move write one row, which exposes something the shifting loops were
hiding: `TopicDao.update` copies *every* mutable field off the BDO —
`sequenceId`, `name`, `description`, `duration` — so the one row a move writes
carries back the name and description that were read with it. A rename
committing in between is lost to an unrelated drag. Every action already names a
single field, so the fix is to write only that field: dirty tracking on the BDO,
whose setters are already the single choke point, or applying the action to the
DAO rather than mutating a BDO and copying it wholesale. Then a move and a
rename on the same topic stop conflicting at all — the same trick ranks play for
ordering, one level down, on columns.

*Why here:* it is the largest structural win available and it depends on nothing
above, but it wants step 4's acknowledgement in place first so the one-event
-per-action property can actually be relied on. It closes the roadmap's
*reordering events are not broadcast* bug by removing the shift rather than by
broadcasting it.

*Done when:* dragging a topic in a long agenda produces a single event, and two
people reordering the same list concurrently end up with the same order.

Step 6 — Operation log, per-block version, transformation (L)
--

This is the roadmap's Phase 3 entry, and the only place a transformation is
needed.

Start by applying remote text events at all: add `TextBlockEvent` to the
`MeetingMessage` union and a `TEXT_BLOCK` branch to the page's handler. Until
that exists there is nothing to make conflict-safe.

Then add `text_blocks.version BIGINT`, bumped only by edits to that block, and a
log of text operations keyed by `(block_id, version)` written in the same
transaction. Clients send the version their edit was written against. If it
matches, apply directly; if not, fetch the operations since that version and
transform the incoming one against each before applying. The transform for a
`{position, length, value}` splice is a pure function of about forty lines,
belongs next to `TextUpdate` in `common/domain`, and is exhaustively testable —
the existing operation shape is already the right one.

The client half is the standard operational-transformation loop:

    pending : the one operation in flight
    buffer  : everything typed since it was sent
    on acknowledgement -> promote buffer to pending and send it
    on remote operation -> transform pending and buffer against it,
                           then apply it locally

The throttle built in `ActionWebSocketClient` is already the send discipline of
that loop, so it carries forward rather than being replaced. What it is missing
is the transform and the buffer.

Two edits to the *same* field still collide once writes are disjoint, and that
is what the version is for. Worth noting the scope is wider than `text_blocks`:
`TopicAction.UpdateName`, `UpdateDescription` and `MeetingAction.UpdateName` are
the same `{position, length, value}` splice against the same kind of stale base.
They do not need the operation log or the transform — `@Version` on the DAO is
enough to turn a lost update into a failed action, which step 4's acknowledgement
can report and the client can resend against fresh state. Retrying only becomes
safe once events are published after commit, or a rolled-back attempt has already
broadcast the events it did not keep.

*Why last:* it is the largest piece, and it is the one that most benefits from
everything above being in place — ordered application, detectable gaps, and a
structure layer that no longer generates conflicts of its own.

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
- **What happens to an action queued behind one that failed?** They were
  composed against a state the server never reached, so they are currently all
  dropped. Once actions carry a base version the server can answer this properly
  instead of the client guessing.
- **Should a rank be computed by the client or the server?** The client knows
  the neighbours it dropped between; the server would have to be told them
  anyway. Client-computed is simpler and standard, and the `(rank, id)` sort
  makes a collision harmless — but it does mean trusting a client-supplied
  ordering key.
- **Moving a block between topics** does not exist yet: `BlockAction.Move`
  carries only a sequence id. Ranks handle it as a one-row write once it does,
  which is a good reason not to scope any version to a topic.
