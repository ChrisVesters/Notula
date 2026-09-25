# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Keeping this file current

This file is maintained, not written once. Update it in the same change that
taught you something — do not leave it for later, and do not wait to be asked.

Update `CLAUDE.md` when:

- **A correction lands.** The user rejects an approach, points out a wrong
  assumption, or answers a question you had to ask. Write down the conclusion
  and the reason for it, so the next instance does not ask again.
- **A convention is discovered.** You had to read three files to work out how
  something is done here. That is a paragraph the next instance should not have
  to derive.
- **A claim here turns out to be wrong.** Fix it in place. A stale instruction
  is worse than a missing one, because it is trusted.
- **The structure moves.** Paths, commands and destinations named below are
  load-bearing; verify one before relying on it, and correct it when it has
  drifted.

Where things go: a durable rule about *how to work in this repo* belongs here.
A war story about how something went wrong, and what it cost, belongs in
`doc/LESSONS.md`. A design decision and its rejected alternatives belong in the
relevant document under `doc/`.

Keep it short. Every line here is read on every task, so prune what has stopped
being true rather than appending to it.

## Design documents

Read these before changing anything in the meeting/collaboration path — they hold
decisions that the code alone does not explain:

- `doc/PRODUCT.md` — what the product is, the domain model, and the constraints
  any implementation has to satisfy.
- `doc/SEQUENCING.md` — the design for action ordering, versioning and conflict
  handling. Explains *why* a change carries what it carries.
- `doc/WEBSOCKETS.md` — the wire protocol as it is today: destinations, and the
  five identifiers (JWT, `userId`, STOMP session id, `client-id`, `change-id`)
  with their different lifetimes.
- `doc/LESSONS.md` — how previous work on this went wrong. The conventions
  section below is drawn from it, and new war stories belong there.
- `doc/ROADMAP.md` — planned work, sized.

## Commands

Backend (from `backend/`):

```
./mvnw clean test                    # the only run whose result may be reported
./mvnw clean verify                  # tests + JaCoCo report
./mvnw spring-boot:run               # port 7000
./mvnw clean test -Dtest=MeetingLockTest
./mvnw clean test -Dtest=MeetingLockTest#methodName
./mvnw test-compile                  # edit loop only — see below
./mvnw dependency:properties org.pitest:pitest-maven:mutationCoverage
```

Frontend (from `frontend/`):

```
npm run dev        # Vite on 5173, but the app talks to nginx on 4443
npm test           # vitest, single run
npm run check      # svelte-check
npm run format     # prettier --write
npm run lint       # prettier --check
```

Running the app locally needs PostgreSQL on `localhost:5432` (db `notula`, user
and password `postgres`; Flyway migrates on startup) **and** the nginx container
from `README.md`, because the frontend is configured against
`https://localhost:4443/api` and `wss://localhost:4443/ws`. Docker is also
required for the backend tests — repository and WebSocket tests run against a
real PostgreSQL through Testcontainers.

`dependency:properties` is part of the mutation-testing command, not decoration:
pitest cannot resolve surefire's `@{argLine}`, so the plugin sets
`parseSurefireConfig=false` and loads the Mockito agent from the property that
goal defines. Without it the run dies with `NoSuchFileException: {argLine}`.

**Never report a test count from an incremental build.** `mvn test-compile` has
reported success while `target/test-classes` still held broken classes from an
earlier failure, producing a green run that a clean build rejected. An
`Unresolved compilation problem` at *runtime* is the tell. Only a number from
`mvn clean test` may be stated.

## Architecture

Two applications behind one nginx origin: a Spring Boot 4 / Java 25 backend
(`backend/`) and a SvelteKit 2 / Svelte 5 frontend (`frontend/`).

### Two transports, split by purpose

- **REST under `/api/*`** — everything outside a live meeting: `users`,
  `sessions`, `organisations`, `organisation-users`, and meeting CRUD.
  `*Controller` classes; errors map to status codes in
  `common/controller/HttpExceptionHandler`.
- **STOMP over WebSocket on `/ws`** — everything inside a meeting. One
  connection per browser tab, authenticated once on the `CONNECT` frame by
  `config/WebSocketChannelInterceptor`. Destinations:
  - `/app/meetings/{id}` — `@SubscribeMapping` in `details/DetailsWebSocket`,
    replies with the full meeting snapshot to that subscriber only.
  - `/app/meetings/{id}/changes` — the *single* inbound destination for every
    change, whatever it is about (`meeting/MeetingWebSocket`).
  - `/topic/meetings/{id}` — every resulting event, broadcast to everyone.
  - `/user/queue/rejections` — refusals only, to the one connection that asked.
    Success is never reported here; it arrives as an event on the topic like
    anyone else's.
  - `/user/queue/acks` — `{id, revision}` for a change that committed, to the
    one connection that sent it. It is the return value of the one handler on
    `/changes`, sent with `@SendToUser`, so it cannot be sent for a change that
    threw: the exception handler answers on `/queue/rejections` instead.

### One envelope each way

`meeting/dto/ChangeDto` is a sealed interface over `MeetingChangeDto`,
`TopicChangeDto`, `BlockChangeDto` and `TextBlockChangeDto`, each with nested
records per operation, carrying the Jackson `@JsonSubTypes` keyed on a `type`
discriminator (`RENAME_MEETING`, `ADD_TOPIC`, `EDIT_TEXT_BLOCK`, …), matched by
`frontend/src/lib/meeting/change/ChangeTypes.ts`.

Events mirror it. `event/dto/EventDto` is the envelope — `origin` and a
`MutationDto` — and `MutationDto` is the same shape of sealed union over
`MeetingMutationDto`, `TopicMutationDto`, `BlockMutationDto` and
`TextBlockMutationDto`, in `event/dto` beside the `event/bdo` mutations they
map (`TextEditDto` stays in `meeting/dto`, shared with the changes), keyed on
the same `type` vocabulary, matched by
`frontend/src/lib/meeting/event/EventTypes.ts`. An event
is named after the change that caused it, so `RENAME_TOPIC` is one word on both
halves of the wire; the two with no inbound counterpart are `ADD_MEETING` and
`REMOVE_MEETING`, because meetings are created and deleted over REST.

**A cross-cutting field goes on `EventDto`, not on the mutations.** That is the
whole point of the envelope: `revision` is one record, not fourteen.
Four per-entity `*EventDto` wrappers over four `*MutationDto` hierarchies,
keyed on a two-level `target` plus `action`, is what this replaced. They were
Lombok getter beans and so serialised alphabetically, where records serialise
in declaration order; everything outbound is a record now, so the expected JSON
in a test is written in declaration order like the inbound half.

A mutation carries its own entity's id and nothing above it — `RENAME_TOPIC`
carries `topic`, `ADD_BLOCK` carries `block` and `topic`, and the meeting
mutations carry no id at all, because the destination already named the
meeting.

**Order is a rank, and the server owns it.** `topics.rank` and `blocks.rank` are
base-62 fractional indexes — `common/domain/Rank`, where lexicographic order
*is* the order, and a rank never ends in the alphabet's first digit so that
`Rank.between` can always find room below one. A change names the sibling it
follows and never a position: `MOVE_TOPIC {topic, afterId}`, `ADD_TOPIC
{afterId, name}`, `afterId` null meaning the front. The service computes the
rank between that neighbour and its current successor, inside the lock, and the
outbound mutation carries the resulting `rank`, read off the saved entity.

Do not move that computation to the client, however convenient the neighbours
are there: two people dropping into one gap compute the same string, and while
`(rank, id)` keeps the order total, nothing can ever be inserted between the two
afterwards. `TopicAction.Move` is not a `TopicAction.Update` for the same
reason — it names a neighbour, not a value, so there is nothing for `apply` to
do.

**There is no `Change` in `bdo`.** A change DTO converts straight to the
entity's `*Action` — `TopicChangeDto.Rename.toBdo()` returns a
`TopicAction.UpdateName` — and `meeting/ChangeService` switches on the DTO and
calls the entity service itself. A parallel `bdo` `Change` hierarchy existed and was
deleted: it restated every `*Action`'s fields, and the three `*ChangeService`
classes that unpacked one into the other held no logic. The two hierarchies
differed only in where identity lived — a field on a change, a parameter on an
action — which is not a difference worth a type.

**The meeting id is never in an action.** It is the scope every change runs in,
so it travels as a parameter the whole way down — `topics.create(origin, scope,
action)`, `blocks.move(origin, scope, blockId, action)` — sourced once from the
destination and turned into a `MeetingScope` by the lock. An `Add` names its parent on the wire only when the
parent is *not* the meeting: `ADD_BLOCK` carries `topic`, `ADD_TOPIC` carries
nothing, because a topic's parent is the meeting the frame was already
addressed to. `TopicAction.Create` used to hold a `meetingId` and it was
removed: it put the same fact on the wire twice with nothing checking the two
agreed, and it leaked into the outbound `TopicMutationDto.Create`, where the
frontend never read it.

A change that carries no payload (`REMOVE_TOPIC`, `REMOVE_BLOCK`) declares no
`toBdo()` at all, so neither sub-interface declares one either.

Variants are grouped by **what `ChangeService` has to do with them**, not by
what they resemble. `TextChangeDto` is a sealed level across the five text
records — `MeetingChangeDto.Rename` / `Describe`, `TopicChangeDto.Rename` /
`Describe`, `TextBlockChangeDto.Edit` — declaring `base()` and `edit()`, because
every one of them is rebased before it is applied. They convert with
`toBdo(Splice rebased)` and have no argument-free `toBdo()`, so a text change
cannot reach a service without being rebased. A `TopicChangeDto.Update` level
over `Rename`, `Describe` and `Schedule` existed so one switch case could serve
all three; it went when two of them needed a rebase the third does not.
`TopicAction.Move` is not a `TopicAction.Update`: it names a neighbour, and
`TopicService.move` resolves it into a rank. Do not "fix" that.

A `TextEditDto` is `@JsonUnwrapped`, so a text edit rides **flat** on the change
(`{"type": "EDIT_TEXT_BLOCK", "block": 9, "position": 4, "length": 12, …}`),
not nested under an `edit` object. The frontend union intersects the type with
`TextEdit` to match. Jackson 3 supports unwrapping into a record creator
property; Jackson 2 did not, so do not port this pattern backwards.

A text edit is a `common/domain/Splice(position, length, value)` below the
wire: the text actions (`TopicAction.UpdateName`, …) extend `TextUpdate`, which
holds one, and the text mutations carry one as `edit`. `TextEditDto` is the
triple on the wire and converts like any DTO — `new TextEditDto(splice)` out,
`edit.toBdo()` in — so a change record is `new TopicAction.UpdateName(
edit.toBdo())`. A generic factory callback lived on `TextEditDto` before
`Splice` existed, to spare spelling the triple out five times, and was removed:
it added a type to save a line per call site.

The five text changes also carry `base`, the revision their positions were
counted in, as a required body field beside the edit — not a header, because
only text edits read it and the positions mean nothing without it
(`SEQUENCING.md` step 6 has the rejected header design). It is a primitive
`long`: Jackson 3 refuses a missing or `null` one rather than reading 0, which
would mean *seen nothing*. It stays out of `TextEditDto`, which the outbound
mutations share and where a base means nothing.

This shape is the point of the rewrite. The previous design gave every mutation
its own endpoint, request type, action, event and publisher per entity, so
**adding one field to every event touched about forty files**. Adding a
cross-cutting field to the envelope is one file. Keep it that way: a new
operation is a new record plus a `@Type` entry, not a new destination.

Flow: `MeetingWebSocket` (one handler, returns the acknowledgement) →
`meeting/ChangeService` (takes the lock, dispatches on the sealed DTO, rebases
a text change through `meeting/TextHistory`, converts to the entity's
`*Action`) → per-entity `*Service` → `*StorageGateway` → Spring Data
repository. `event/EventService` emits and logs the events.

**`TextHistory` rebases inside the lock.** It reads the logged events after the
change's `base` (`EventStorageGateway.findAllSince`), keeps those that edited
the same text — the mutation the change is named after, on the same entity —
and moves the incoming `Splice` over each in order. A base that is not below the
change's own revision is refused as invalid. The lock is what makes that read
correct: no other change on the meeting can log an event between it and the
write. The entity services never see the log; they apply the edit the server
actually made, and publish that.

**`ChangeService` is the boundary of a change.** It is the one place the lock is
taken, so the revision a change committed at is a return value — the
`MeetingScope` — and the handler turns it into an `AcknowledgedDto`. It is not
one of the per-entity `*ChangeService` classes that were deleted: those unpacked
a `bdo` `Change` into an `*Action` and held no logic, where this one owns the
lock, the dispatch and the scope every service below it runs in.

**One event service, one event type.** A service builds the outcome of its
change as a mutation — `BlockMutation.Move(blockId, rank)`, read off the saved
entity — wraps it in an `EventInfo(scope, origin, mutation)` and calls
`EventService.publish(event)`, which stores it and sends the stored event. The
mutations are shaped like the outcome, not the request, which is what the log
can hold. They all live in `event/bdo`, under a sealed `Mutation`, so the
switch that maps them to their DTOs (`MutationDto.of`) is exhaustive with no
`default`. That is why they are not in their entities' `bdo` packages: a sealed
type can only permit subtypes in another package inside a named module, and the
app is in the unnamed module. Both other arrangements were built and dropped —
the groups beside their entities with an unsealed marker interface (a switch
ending in `default -> throw`), and a generic `Event<M>` (the same switch, with
nothing tying `M` to a mutation).

`EventInfo` is a class like the other `*Info`s, not a record, because its id
is assigned by the database: `getId()` refuses until it is set.

Before this: four `*Event` records of `(*Info, *Action, Origin)` with one
`publish` overload each. They held the request, the log can hold only the
outcome, and `*Action.Delete` existed only to build them. Before that, four
per-entity `*Publisher` classes, each re-reading a meeting id the caller
already had.

**Every event is logged, as sent.** `EventStorageGateway.create(EventInfo)`
works like every other gateway — `new EventDao(event)`, save, `saved.toBdo()` —
in the change's own transaction, so the log and the broadcast cannot disagree.
`EventInfo` holds the origin expanded — `userId` and `clientId`, nullable — not
an `Origin`, and no organisation: an event is only ever reached through its
meeting, inside a change that has already been authorised, so the organisation
would be stored and never read. The `clientId` stays because the broadcast
needs it for `isOwnEvent`. The DAO keeps both as columns, and its `mutation` field is the
`MutationDto` itself, mapped by Hibernate with `@JdbcTypeCode(SqlTypes.JSON)`
through its Jackson 3 format mapper — the DAO holds no `JsonMapper` of its own.
The rest of the envelope is rebuilt from the row. The column is `JSONB`, which
reorders keys; nothing depends on their order, and `EventRepositoryTest`
round-trips every mutation type through the database. A meeting's delete publishes
before it deletes, because the event row needs the meeting row.

### Ordering and consistency

Consistency is a property of a *meeting*, not of an entity. Four mechanisms
enforce it, in order:

1. `config/SessionOrderInterceptor` + `SessionOrder` — serialise the frames of a
   single STOMP session, so two SENDs from one tab cannot be handled
   concurrently. Inbound frames run on a dedicated `ws-inbound-` pool configured
   in `config/WebSocketConfig`, never on the two-thread scheduler that also
   sends broker heartbeats.

   **The wait is unbounded and the gate never refuses.** A semaphore hands out
   a permit on every release, so releasing one that was never acquired leaves
   the session holding two and it stops being ordered for the rest of the
   connection. `SessionOrder.Turn` therefore hands a permit back at most once
   per turn taken. Slowness is not this class's to report: `MeetingLock` already
   times out into `BusyEntityException`, which the advice refuses as retryable.

   The turn is handed back in **both** `afterMessageHandled`, for a frame that
   reached the handler, and `afterSendCompletion` when the frame was not sent,
   for one a later interceptor refused. Missing the second leaks a permit and
   stalls the session for good, which is why the wait used to be bounded. Do not
   give this class a way to answer the client: everything that can talk to a
   connection is built from the channels, which are built by asking the
   configurers for their interceptors, so any such dependency is a bean cycle.
2. `meeting/MeetingLock` — a per-meeting `ReentrantLock` that **owns the lock,
   the transaction and the revision**: it acquires, bumps `meetings.revision`,
   and runs the action inside a
   `TransactionTemplate` with a `bdo/MeetingScope` — the meeting id and the new
   revision. **`meeting/ChangeService` is its only caller on the change path**,
   so a change takes it exactly once; `MeetingService.delete` takes it too,
   because a meeting is deleted over REST. The entity services take a
   `MeetingScope` and cannot be reached without one, which is what replaced
   each of them locking for itself. Timing out raises
   `BusyEntityException`, which is rejected as *retryable*. `run` and `call`
   both bump; the plain lock underneath them is private, so a non-bumping pair
   is two lines when a read path needs one. They are not one overloaded
   `change` because two overloads over an implicitly typed lambda are
   ambiguous.

   **One revision per change, not per event**, and the scope travels as a
   parameter — `move(origin, scope, topicId, action)`, then
   `events.publish(scope, event)` — for the same reason the meeting id used to,
   and because nothing downstream can re-read it: a `REMOVE_MEETING` deletes
   the row its revision came from. Publishing stays in the services: handing
   the action a publisher already bound to the scope was considered and
   rejected, because the lock then owns the whole boundary a change runs in and
   stops being a lock. Do not offer it as a cleanup.

   **The revision leaves as a return value.** `ChangeService.apply` returns the
   `MeetingScope` the change committed at and the handler acknowledges it. An
   `ExecutorChannelInterceptor` was built first and removed: the interceptor
   holds the frame but not the handler's return value, so the revision had to
   reach it on a thread-local, and it could not hold a `SimpMessagingTemplate`
   either — the channels are built from the interceptors and the template is
   built from the channels, so that is a bean cycle. Both problems were the
   same thing telling us the acknowledgement is not interceptor work.

   The bump is a read, a change to the object and a write — `find`,
   `MeetingInfo.bumpRevision`, `update` — like any other change to a meeting,
   and `MeetingDao.update` carries the number to storage with the name and the
   description. `MeetingStorageGateway` keeps no `bumpRevision` of its own; a
   `bumpRevision` on the DAO and a `MeetingRevision` component over the
   repository were both tried and removed.

   The snapshot has to be comparable to the stream, so `DetailsService.get`
   runs as one `REPEATABLE READ` read-only transaction. Without it the N+1
   reads each see their own database snapshot, and the revision reported can
   belong to a state the payload has already moved past.

   The meeting id comes from the destination and is **passed down** — to
   `ChangeService.apply(origin, meetingId, change)`, and below the lock as the
   `MeetingScope` — so the lock is taken before anything is read. Do not
   reintroduce a `getMeetingId` that derives it from the entity: that read
   happens outside the lock. The tree walk stays, as
   the *authorisation* check inside `getById(principal, meetingId, entityId)`.
3. `common/messaging/TransactionalPublisher` — events are sent `afterCommit`, so
   no client ever sees an event for a change that rolled back.
4. `common/controller/WebSocketExceptionHandler` — maps exceptions to
   `RejectedDto` on `/user/queue/rejections`, carrying the client's `change-id`
   and whether it is worth retrying. Note that Spring's
   `AbstractMethodMessageHandler` logs and swallows handler exceptions
   regardless of whether a `@MessageExceptionHandler` exists; do not assume an
   exception thrown in a handler reaches an interceptor.

Cross-cutting per-frame values (`Origin`, `ChangeId`) are resolved by
`HandlerMethodArgumentResolver`s registered in `WebSocketConfig`
(`OriginArgumentResolver`, `ChangeIdArgumentResolver`) — not by thread-locals and
not by repeating `@Header` on every handler method. The frontend's
`WebSocketClient` is a dumb transport: callers pass the headers they need.

### Layering and package shape

Every domain package is `<domain>/` with `bdo/`, `dao/` and `dto/` beneath it:

- **`dto`** — transport. Validated with Jakarta Validation; converts to domain
  with `toBdo()`.
- **`bdo`** — business domain objects. **Data, not behaviour** — records and
  sealed interfaces. Logic lives in services. The exception that earns its
  place is a value type whose own arithmetic belongs to it: `common/domain/Rank`
  computes a rank between two ranks, as `Splice` applies and rebases a text
  edit.
- **`dao`** — JPA entities.

Conversions are explicit at each boundary. `*StorageGateway` wraps the Spring
Data repository and does the DAO ↔ BDO conversion; services never see DAOs.

**`*Dao.update` copies every mutable field, on purpose.** The DAO is a whole
mirror of the BDO, and the BDO is what guards the logic — do not add dirty
tracking to make a write partial. The lost update that would justify it needs
two writers on one meeting at once, which `MeetingLock` prevents; when that
stops holding, the answer is optimistic locking, not a partial write.

Every row below the organisation carries `organisation_id` directly, so
authorisation is a direct check rather than a tree walk. `events` is the
exception: it is never looked up on its own, only by meeting, so it has no
`organisation_id`. Do not add a
`meeting_id` column to `blocks` or a `meeting-id` header to actions — both have
been tried and reverted; walk the tree instead.

### Frontend

Route groups mirror the access model: `(public)` for login and registration,
`(unscoped)` for choosing an organisation, `(scoped)` for everything inside one,
`(scoped)/(admin)` for administration. `src/lib/<domain>/` holds the API client,
WebSocket client and views per domain. `MeetingWebSocketClient` is the single
entry point for meeting traffic; it mints a `change-id` per change and returns
it. It keeps one change in flight and queues the rest, and sends nothing
before the first snapshot. It releases the next change only once the
acknowledgement has arrived **and** the revision it names has been applied —
they come on different subscriptions, and releasing on the acknowledgement
alone would stamp a base the page does not hold yet. A text change is stamped
with `base`, the page's revision, as it leaves the queue. A refusal drops both
the change and the queue — whatever was waiting was composed against a change
that never happened — and reloads, because the page still shows the refused
edit. A snapshot drops the queue too: what was waiting was written against text
no longer on screen. Nothing
is ever resent: whether a change in flight committed is unknowable, and a text
edit applied twice corrupts where a lost one does not.

**A list owns its `<li>`; an item component does not render one.** Wrapping
itself in an `<li>` ties a component to one kind of parent. `common/ReorderList`
renders the `<ul>`/`<li>` and owns drag-and-drop: it knows the order, so it
turns a drop into an `afterId` and sends nothing for a drop that moves nothing.
Items only mark a `data-reorder-handle` and take no `previousId`.

**A lost connection is not recovered from, on purpose.** `WebSocketClient` sets
`reconnectDelay: 0`, overriding stompjs's default of five seconds. Reconnecting
silently is worse than staying down: it replays the subscription map, which
re-fires the snapshot and rebaselines the page over whatever the sender never
managed to send, so a note-taker watches their own text vanish with nothing
said. A tab that stops updating is at least a symptom someone can act on. Do
not switch the retry back on by itself — the roadmap's *Reconnect and recover*
and *Sync status* are what make recovery safe and visible, and they go
together. Note that `onDisconnect` is no signal for a lost link either:
stompjs only fires it on a DISCONNECT receipt, which by its own documentation
may never arrive when the connection is interrupted.

The meeting page applies every event to its state, and `editor/Input.svelte`
and `editor/TextArea.svelte` are bound to that state, so someone else's edit
reaches an editor as a new `value`. Svelte then writes it to the element, which
puts the caret at the end. Both editors therefore compare the element's current
text with the incoming value in `$effect.pre`: equal means the user typed it
and the DOM is already right, different means it came from elsewhere and the
caret is moved across the change by `editor/TextEdit.moved` after a `tick`.
The echo of our *own* text edit is dropped by `MeetingWebSocketClient` before
the page sees it, because the editor already applied it; structural echoes are
not, since create, move and delete still rely on them. It is an echo only while
the change in flight is *pending* — in the page but not yet in a revision the
page has applied. After a snapshot has replaced the page, the change in flight
is no longer on screen, so its echo is applied like anyone else's.

A remote text edit is rebased over the page's own edits the server has not put
in a revision yet — the pending one in flight, then the queue — and they over
it, with `editor/TextEdit.rebased`. The remote edit goes first at a tie
(`first = true`), because the server applied it first.

`MeetingWebSocketClient` owns the event stream, and the page only applies what
it is handed. The client checks `revision` on every event before handing it on,
and needs **two** pieces of state to do it — `revision` and `streamed`. All events of one change
carry that change's revision, so a second event at the current revision is the
same change continuing and must be applied; an event at the revision a
*snapshot* reported is already in the payload and must be dropped. One cursor
cannot tell those apart. So: below the current revision is stale, equal is the
same change only when `streamed`, `+ 1` is the next change, beyond that is a gap
and resyncs. Testing `=== revision + 1` alone makes every drag look like a gap.
Events arriving before the snapshot are buffered and replayed once the snapshot
sets the baseline. Resync is `MeetingWebSocketClient.resync`, called by the
client on a gap and by the page when an event names something it does not hold;
clearing `revision` is what marks one as in flight, so the several events of one
change resync once. A new operation needs no client-side
plumbing to be gap-checked; it needs its events published under the change's own
`MeetingScope`, which `MeetingLock` already guarantees.

**One change, one event, one revision.** This holds now that ranks removed the
sibling shifting, and it is what the gap check rests on: the revision numbers a
change, so if a change could still arrive in pieces the check would be blind to
receiving part of one. A new operation therefore writes a single row and
publishes a single event; one that wants to publish a list of them is a question
rather than a judgement call. Do not close any remaining gap by numbering events
within a change.

**A change that accepts and changes nothing still publishes.** A move to where
something already sits is accepted, so it spends a revision, so it has to be
broadcast — otherwise the next change arrives two above what every other client
holds and they all resync. Refusing it was the other candidate: a request for a
state that already holds is not an error, and a refusal costs the sender the
changes queued behind it. `ReorderHandler.handleDrop` still filters it
client-side to save the round trip, but the invariant no longer rests on that.

## Conventions

These have each been violated and reverted at least once. **A design decision
that departs from an established convention is a question, not a judgement
call — ask before writing.**

- **Never publish an Artifact.** Documents, designs, reports and diagrams
  go to a file on disk, and the reply names the path. A design document was
  published to claude.ai once and the project then surfaced in someone else's
  Claude usage. This rule outranks any skill that asks for an artifact, and
  publishing is not to be offered as an alternative either.
- **Work in small, committed steps.** A change that spans layers is planned as
  a sequence of steps. Each step compiles, passes `mvn clean test`, moves its
  design doc along with the code, and stops for review before the next. Do not
  start a refactor on top of uncommitted feature work. Never delete a file that
  has local modifications. Text rebasing was built in one sixty-file piece with
  a refactor on top, and had to be stashed whole: `doc/LESSONS.md`, *Step 6, the
  first time*. The design and the order to rebuild it are in `SEQUENCING.md`
  step 6.
- Package names are `bdo` / `dao` / `dto`. Not `domain` / `store` / `web`.
- **There is one migration, `V1__init.sql`, and a schema change edits it.**
  Nothing is deployed yet, so there is no history to preserve: a `V2__*.sql`
  was written for the ranks change and folded back in. Fold the change into
  `V1`, update the seed data under `src/test/resources/db/`, and drop the
  database locally rather than adding a file Flyway would have to replay.
- Primary keys are server-assigned `BIGINT`. No client-generated ids as keys;
  client-minted UUIDs are correlation handles (`Submission.id`, `change-id`) and
  stay UUIDs.
- `bdo` types carry data. Do not give them behaviour. A mutator that only
  maintains its own field — `MeetingInfo.bumpRevision`, next to its setters —
  is the edge of that, not a licence to put a rule in a record.
- Do not comment classes and methods. No Javadoc, no summary line restating
  the signature, no section banners. The code says what it does; make it
  readable instead — a clearer name, a smaller method, an extracted variable.
  The only comment worth writing explains a *reason* the code cannot show:
  `MeetingLock`'s `claim`/`release` is the model.
- **An identifier is named `<thing>Id`, never `<thing>`.** A `long topic` is a
  topic id, not a topic, and the name has to say so — on a record component, a
  parameter, a local and the JSON field it serialises to. `TopicMutationDto`'s
  `topic` / `block` components are the old shape and are wrong; name new ones
  `topicId` / `blockId`. Only a field holding the entity itself gets the bare
  name.
- Name variants by what separates them, not by what they share. Rejections are
  built with `RejectedDto.permanent` / `.temporary`, not `refused` — every
  rejection is a refusal, so that name distinguishes nothing. `transient` was
  the first choice and is a Java keyword.
- Java: tabs, 80-column line split, `final` on parameters and locals,
  constructor injection. **Write code that already matches
  `backend/java-formatter.xml`.** That Eclipse config is what correct means
  here, so read it when unsure rather than guessing. Do not run a formatter and
  do not add tooling to run one. Its two least
  guessable rules: it **never breaks at `=`**, and never inside annotation
  arguments, so a long initialiser breaks inside the expression, or overruns 80
  columns when it cannot, instead of wrapping after the `=`. Imports are grouped
  `java` / `javax` / `jakarta` / `org` / `lombok` / `com`, statics first,
  alphabetical within a group.
- TypeScript/Svelte: tabs, 80 columns, double quotes, no trailing commas —
  `prettier` decides, run `npm run format`.
- Commit messages: `<issue number> - <Sentence.>`, e.g.
  `94 - Sequence actions by session on backend.`
- All UI text goes through `src/lib/assets/translations/`.

## Tests

- `test/ControllerTest`, `test/RepositoryTest` and `test/WebSocketTest` are the
  base classes; `WebSocketTest` drives real STOMP frames against a random port
  with a mocked `JwtDecoder`. `connect` returns the `StompSession` and may be
  called more than once, so a test can watch what a *second* subscriber
  receives; `subscribe` and `send` take a session, or default to the first one
  connected. `FrameHandler.getResponse` is the first frame only — use
  `await(count, timeout)` for several frames — and asking for one more than
  expected is how a test proves a change published no others. It returns what arrived rather than throwing, so assert on the size.
- **Nothing orders frames across two connections.** A test that subscribes on
  one session and sends on another must know the subscription is registered
  before it sends, or the broadcast goes to nobody and the test fails only on a
  slower machine. There is no receipt to wait on: the simple broker sends no
  `RECEIPT` for a `SUBSCRIBE`, so `addReceiptTask` never fires — verified, after
  configuring the `TaskScheduler` its absence otherwise complains about.
  `MeetingChangeWebSocketTest.observing` is the pattern: subscribe, then ask the
  same session for the `/app/meetings/{id}` snapshot and wait for the reply, a
  round trip the earlier subscription cannot still be behind.
- A WebSocket test that touches the database seeds with `@Sql` and must list
  `/db/clean.sql` first: `@SpringBootTest` does not roll back, so without the
  truncate the second test method fails on a duplicate key.
- Test names are **short identifiers, not descriptions**. A `@Nested` class
  names the method under test; each `@Test` names the case in a word or two —
  `success`, `value`, `unnamed`, `otherMeeting`, `timeout`. Not
  `namesTheChangeItCouldNotRead`. The assertions describe the test; the name
  only has to tell it apart from its siblings.
- **Duplicate assertions rather than extract them.** Each test spells out its
  own verifications inline, even when the next test repeats them word for
  word: a test is read on its own, and a `verifyCreated(...)` helper hides
  what it checks. The DRY instinct is for production code. Assertions that
  hold for every successful case go *in* every successful case, not in a
  separate `event` or `oneRow` test. The same goes for setup: a private helper
  in a test class hides steps too — `EventDaoTest.saved(dao)`, which set the id
  by reflection, was inlined into each test the way `TopicDaoTest` does it.
- `Test<Entity>` classes hold shared fixtures; `<Entity>Matcher` extends
  `test/Matcher` for structural assertions.
- No test-only accessors on production classes. A method that exists so a test
  can read internal state is a smell in the class, not a convenience: assert the
  behaviour the state produces instead. `SessionOrder.tracked()` was exactly
  this and was removed.
- Seed data for repository tests lives in `src/test/resources/db/*.sql`.
- A serialisation test lives in the test class of the type it serialises.
  `EventDtoTest` asserts the *envelope* — `origin` plus one representative
  mutation — and each `*MutationDtoTest` has a `Serialise` nested class for its
  own records. Fifteen per-mutation envelope tests once sat in `EventDtoTest`;
  they moved out.
- Serialisation tests assert on the JSON **string**, not on a parsed tree.
  `assertThat(json).isEqualToIgnoringWhitespace("""…""")` against a text
  block — never `readTree` and walk `JsonNode`s. Field names, nesting and
  absent-vs-null are part of the wire contract, and a tree walk asserts none of
  them. Bean DTOs serialise their properties **alphabetically**, records in
  declaration order, so write the expected document in that order.
- Verify claims about framework behaviour by running something — printing the
  thread name, counting queries, throwing the exception and watching where it
  goes. Reading the code has repeatedly given the wrong answer here.
- After a find-and-replace sweep, read the diff. Compiling is not evidence: a
  `UUID id` → `long id` sweep silently converted a correlation handle, and a
  JSON-template edit shifted `.formatted(...)` arguments so a meeting id was
  passed as a sequence id.

## Known gaps

Stated in `doc/LESSONS.md` and worth knowing before starting:

- One WebSocket test reaches the database, and only one.
  `MeetingChangeWebSocketTest` mocks nothing below the web layer and drives a
  topic move, a schedule and two concurrent renames to a second subscriber, so
  lock, transaction, log, rebase, publisher and revision are exercised together
  for those. Every other
  `WebSocketTest` subclass still makes each entity service a `@MockitoBean`, so
  no other operation is covered end to end.
- Concurrent text edits converge, with the gaps `SEQUENCING.md` step 6 names:
  an IME composition is sent at `compositionend`, so a remote edit applied
  mid-composition lands in text nobody has described; the event log is never
  pruned; and a reload that drops unsent edits says nothing.
- The frontend has almost no tests: `frontend/tests/` covers a few form
  components, one API client, one editor component, the text-edit splice and
  rebase, and `MeetingWebSocketClient`'s queue and event stream, and nothing
  else of the meeting path — the page's `mutate` is untested.
  Configuration has stopped the suite twice, so treat any frontend coverage
  claim as unverified until you have run `npm test` and read the file count it
  prints: `vite.config.ts`'s `include` globs must point at `tests/`, not
  `src/`, and a run that collects nothing still exits zero.
- **The directory is `tests/`, plural, and not by taste.** SvelteKit hardcodes
  that name when it generates `.svelte-kit/tsconfig.json` — *"we advocate
  putting tests in a top-level tests folder and it's not configurable"*. It sat
  in `test/` and so no test file was in the TypeScript project at all: `$lib`
  did not resolve in the editor, and `npm run check` reported zero errors
  having never looked. Renaming it back means hand-maintaining an `include` in
  `tsconfig.json`, because extending replaces that field rather than adding to
  it.
- The account layer (organisations, users, credentials, sessions) is the
  original shape and was deliberately left untouched by the rewrite.
