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

### One change envelope

`meeting/dto/ChangeDto` is a sealed interface over `MeetingChangeDto`,
`TopicChangeDto`, `BlockChangeDto` and `TextBlockChangeDto`, each with nested
records per operation, carrying the Jackson `@JsonSubTypes` keyed on a `type`
discriminator (`RENAME_MEETING`, `ADD_TOPIC`, `EDIT_TEXT_BLOCK`, …), matched by
`frontend/src/lib/meeting/change/ChangeTypes.ts`.

**There is no `Change` in `bdo`.** A change DTO converts straight to the
entity's `*Action` — `TopicChangeDto.Rename.toBdo()` returns a
`TopicAction.UpdateName` — and `MeetingWebSocket` switches on the DTO and calls
the entity service itself. A parallel `bdo` `Change` hierarchy existed and was
deleted: it restated every `*Action`'s fields, and the three `*ChangeService`
classes that unpacked one into the other held no logic. The two hierarchies
differed only in where identity lived — a field on a change, a parameter on an
action — which is not a difference worth a type.

**The meeting id is never in an action.** It is the scope every change runs in,
so it travels as a parameter the whole way down — `topics.create(origin,
meetingId, action)`, `blocks.move(origin, meetingId, blockId, action)` — sourced
once from the destination. An `Add` names its parent on the wire only when the
parent is *not* the meeting: `ADD_BLOCK` carries `topic`, `ADD_TOPIC` carries
nothing, because a topic's parent is the meeting the frame was already
addressed to. `TopicAction.Create` used to hold a `meetingId` and it was
removed: it put the same fact on the wire twice with nothing checking the two
agreed, and it leaked into the outbound `TopicMutationDto.Create`, where the
frontend never read it.

A change that carries no payload (`REMOVE_TOPIC`, `REMOVE_BLOCK`) declares no
`toBdo()` at all, so neither sub-interface declares one either.

Variants are grouped by **the service method they route to**, not by what they
resemble. `TopicChangeDto.Update` is a sealed level over `Rename`, `Describe`
and `Schedule` declaring `topic()` and `toBdo()`, which is what lets one switch
case serve all three. `Move` is deliberately *not* under it even though
`TopicAction.Move` is a `TopicAction.Update` in the `bdo` — it goes to
`TopicService.move`, which resequences siblings. Do not "fix" that.

A `TextEditDto` is `@JsonUnwrapped`, so a text edit rides **flat** on the change
(`{"type": "EDIT_TEXT_BLOCK", "block": 9, "position": 4, "length": 12, …}`),
not nested under an `edit` object. The frontend union intersects the type with
`TextEdit` to match. Jackson 3 supports unwrapping into a record creator
property; Jackson 2 did not, so do not port this pattern backwards.

`TextEditDto` is data only: it names the `(position, length, value)` triple on
the wire and knows nothing about `bdo`. Each change record maps the triple onto
its own action itself — `new TopicAction.UpdateName(edit.position(),
edit.length(), edit.value())`. A generic factory callback lived on `TextEditDto`
to avoid writing that out five times and was removed: it added a type to spare a
line per call site, and mapping is the change record's job.

This shape is the point of the rewrite. The previous design gave every mutation
its own endpoint, request type, action, event and publisher per entity, so
**adding one field to every event touched about forty files**. Adding a
cross-cutting field to the envelope is one file. Keep it that way: a new
operation is a new record plus a `@Type` entry, not a new destination.

Flow: `MeetingWebSocket` (dispatches on the sealed DTO, converts to the
entity's `*Action`) → per-entity `*Service` → `*StorageGateway` → Spring Data
repository. `*Publisher` classes emit the events.

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
2. `meeting/MeetingLock` — a per-meeting `ReentrantLock` that **owns both the
   lock and the transaction**: it acquires, then runs the action inside a
   `TransactionTemplate`. Mutating `*Service` methods go through it. Timing out
   raises `BusyEntityException`, which is rejected as *retryable*.

   The meeting id comes from the destination and is **passed down** as a
   parameter — `move(origin, meetingId, topicId, action)` — so the lock is taken
   before anything is read. Do not reintroduce a `getMeetingId` that derives it
   from the entity: that read happens outside the lock. The tree walk stays, as
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
  sealed interfaces. Logic lives in services.
- **`dao`** — JPA entities.

Conversions are explicit at each boundary. `*StorageGateway` wraps the Spring
Data repository and does the DAO ↔ BDO conversion; services never see DAOs.

Every row below the organisation carries `organisation_id` directly, so
authorisation is a direct check rather than a tree walk. Do not add a
`meeting_id` column to `blocks` or a `meeting-id` header to actions — both have
been tried and reverted; walk the tree instead.

### Frontend

Route groups mirror the access model: `(public)` for login and registration,
`(unscoped)` for choosing an organisation, `(scoped)` for everything inside one,
`(scoped)/(admin)` for administration. `src/lib/<domain>/` holds the API client,
WebSocket client and views per domain. `MeetingWebSocketClient` is the single
entry point for meeting traffic; it mints a `change-id` per change and returns
it.

## Conventions

These have each been violated and reverted at least once. **A design decision
that departs from an established convention is a question, not a judgement
call — ask before writing.**

- **Never publish an Artifact.** Documents, designs, reports and diagrams
  go to a file on disk, and the reply names the path. A design document was
  published to claude.ai once and the project then surfaced in someone else's
  Claude usage. This rule outranks any skill that asks for an artifact, and
  publishing is not to be offered as an alternative either.
- Package names are `bdo` / `dao` / `dto`. Not `domain` / `store` / `web`.
- Primary keys are server-assigned `BIGINT`. No client-generated ids as keys;
  client-minted UUIDs are correlation handles (`Submission.id`, `change-id`) and
  stay UUIDs.
- `bdo` types carry data. Do not give them behaviour.
- Do not comment classes and methods. No Javadoc, no summary line restating
  the signature, no section banners. The code says what it does; make it
  readable instead — a clearer name, a smaller method, an extracted variable.
  The only comment worth writing explains a *reason* the code cannot show:
  `MeetingLock`'s `claim`/`release` is the model.
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
  with a mocked `JwtDecoder`.
- Test names are **short identifiers, not descriptions**. A `@Nested` class
  names the method under test; each `@Test` names the case in a word or two —
  `success`, `value`, `unnamed`, `otherMeeting`, `timeout`. Not
  `namesTheChangeItCouldNotRead`. The assertions describe the test; the name
  only has to tell it apart from its siblings.
- `Test<Entity>` classes hold shared fixtures; `<Entity>Matcher` extends
  `test/Matcher` for structural assertions.
- No test-only accessors on production classes. A method that exists so a test
  can read internal state is a smell in the class, not a convenience: assert the
  behaviour the state produces instead. `SessionOrder.tracked()` was exactly
  this and was removed.
- Seed data for repository tests lives in `src/test/resources/db/*.sql`.
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

- No WebSocket integration test reaches the database. `WebSocketTest` subclasses
  drive real STOMP frames through the full messaging stack, but every entity
  service is a `@MockitoBean`, so nothing exercises lock, transaction and
  publisher together.
- Text conflicts are refused with a retryable rejection rather than merged;
  transformation is the seam to plug into.
- The frontend has almost no tests: `frontend/test/` covers three form
  components and one API client, 17 in total. The suite could not start at all
  until the Vitest browser provider was fixed, so treat any frontend coverage
  claim as unverified until you have run `npm test`.
- The account layer (organisations, users, credentials, sessions) is the
  original shape and was deliberately left untouched by the rewrite.
