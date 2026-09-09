Lessons learned
=

From rewriting Notula's collaboration core. Written down because most of these
cost something to find out, and the expensive ones were not about the domain.

`PRODUCT.md` holds what the system is and the design conclusions. This document
is about how the work went: what was worth doing, what was wasted, and what to
do differently.

The short version
==

The design analysis was sound and the implementation was repeatedly wrong in the
same three ways: asserting things instead of measuring them, trusting an
incremental build, and making design decisions the project had already made.
None of those are domain problems. All of them are cheap to avoid.

What the rewrite was for
==

The first implementation modelled every mutation as its own endpoint, request
type, action, event and publisher, for each of four entity types. Consistency
and concurrency, though, are properties of a *meeting*. Every cross-cutting
concern — who made a change, which position it landed at, acknowledging it,
ordering it — then had to be threaded through all of those types.

The measurement that settled it: **adding one field to every event touched about
forty files.** In a design where changes share one envelope, that is one file.

That number is the useful diagnostic, not the architecture opinion. When adding
a cross-cutting concern costs a linear sweep of the codebase, the concern is not
complex — the plumbing is.

Measure before asserting
==

Three claims made from reading code turned out to be wrong, and each was settled
in minutes by running something.

- **"The ordering gate blocks a dedicated inbound pool thread."** It did not.
  There was no `clientInboundChannel` pool at all: the channel had fallen back
  to `webSocketTaskScheduler`, a **two-thread** executor that also sent the
  broker heartbeats. Two blocked actions would have frozen heartbeating
  server-wide. One `println` of `Thread.currentThread().getName()` found it.
- **"Deleting the exception handler will let the exception reach the
  interceptor."** It does not. Spring's `AbstractMethodMessageHandler` logs and
  swallows handler exceptions whether or not a `@MessageExceptionHandler`
  exists. The proposed fix would have changed nothing.
- **"The acknowledgement reports failures."** It never had. Because of the
  above, the interceptor always saw a null exception, so every failed action was
  acknowledged as `APPLIED`. The unit test passed the exception in directly and
  so could not catch it.

The last one is the worst: it was found only because a reviewer asked an
unrelated question. **A framework's behaviour is not obvious from its API.**
Print the thread, count the queries, throw the exception and watch where it
goes.

This eventually became a habit worth keeping: `MeetingCostTest` counts prepared
statements per change and asserts exact numbers, so a read path that quietly
multiplies queries fails in the suite rather than in production.

Never trust an incremental build
==

`mvn test-compile` reported success while `target/test-classes` still held
broken classes from an earlier failure. A run was reported as **759 tests
green** that a clean build rejected outright. It happened twice.

The Eclipse compiler's `Unresolved compilation problem` appearing at *runtime*
is the tell — that is a stale class, not a real failure.

**Only report a number that came out of `mvn clean test`.** An incremental build
is for the edit loop, never for a claim.

Conventions beat preferences
==

The single most repeated mistake. Every one of these was reverted:

| Introduced | Why it was wrong |
| --- | --- |
| `domain` / `store` / `live` / `web` packages | The project uses `bdo` / `dao` / `dto`. Two conventions in one repo is worse than a slightly weaker word. |
| Client-generated UUID primary keys | The schema uses server-assigned `BIGINT` everywhere else, and a key from an untrusted client is a surprise. |
| A rich domain model with behaviour | `bdo` here means data; logic lives in services. |
| A `meeting-id` header on every action | It saved one read on topic actions and none on blocks, in exchange for trusting a client-supplied lock key. |

The pattern is the same each time: an *arguably* better choice, made silently,
in a codebase that had already chosen. The cost is not the choice — it is that
the codebase now has two of everything until someone finishes the sweep.

**A design decision that departs from an established convention is not a
judgement call. It is a question.** Ask it before writing, not after.

Round trips are the expensive unit
==

The `meeting-id` detour is the clearest waste in the whole exercise: proposed,
implemented across roughly forty files, then reverted to exactly where it
started. The objection that killed it was available before any code was written
— for blocks, the tree walk happened *inside* the ownership check anyway, so
the header bought nothing there.

**Cost a change by what it makes true, not by what it makes faster.** "One fewer
query on one of four entity types" was never going to pay for a client-supplied
primary key.

Blanket edits need narrow anchors
==

Three self-inflicted bugs, all from regexes applied wider than intended:

- `UUID id` → `long id` also converted `Submission.id`, a correlation handle
  that was correctly a UUID.
- `state` → `live` also rewrote the import `$app/state`.
- Stripping `"meetingId": %d,` from JSON templates without dropping the matching
  `.formatted(...)` argument shifted every remaining placeholder, so
  `sequenceId` silently received the meeting id.

Only the last was caught by tests, and only because it changed a *value* rather
than a type. **After a sweep, read the diff — do not just check that it
compiles.**

A reviewer's question is often a bug report
==

"Doesn't the websocket already acknowledge delivery?" led to finding that
acknowledgements never reported failure. "Why did you name the packages domain
and store?" was a convention violation. "This uuid is generated by the client,
which I don't like" was an unasked-for schema decision.

None were phrased as defect reports. All of them were.

**Treat a question about a design choice as a prompt to go and check it, not to
explain it.** The explanation is cheap and usually wrong.

What worked
==

Worth keeping, not just the failures:

- **Naming the root cause rather than patching symptoms.** The granularity
  mismatch — a fine-grained RPC domain model against document-shaped
  concurrency — explained every piece of machinery that had accumulated, and
  the rewrite followed from it.
- **Writing the design down before rebuilding.** `PRODUCT.md` separated what the
  product is, what real-time has to guarantee, and what was learned, so the
  rewrite could satisfy the requirements freely instead of reproducing the old
  shape.
- **Testing the claim the design rests on.** Eight writers released from a
  barrier through the real service and Postgres, asserting revisions 1–8 with no
  duplicates. The ordering guarantee is a row lock, so it cannot be tested with
  anything mocked.
- **Deleting rather than accumulating.** 161 files removed. The rewrite is
  smaller than what it replaced: 36 backend classes for the meeting, against
  four entity packages before.

Where it ended up
==

Backend rewritten and green — 428 tests, `mvn clean test`. Frontend rewritten
around one connection and one change stream. Per-change database cost pinned by
a test, and independent of meeting size.

Still open, and stated plainly rather than buried:

- **The account layer is untouched.** Organisations, users, credentials and
  sessions are still the original shape; the schema was kept compatible with
  them deliberately.
- **No WebSocket integration test.** The change service and storage are covered
  against real Postgres, but nothing drives a STOMP frame end to end.
- **Text conflicts fail rather than merge.** An edit composed against an older
  version of a block is refused with a retryable rejection. That is strictly
  better than silent corruption, and it is not the transformation the product
  needs. It is the seam that one plugs into.
- **The frontend has no tests at all.**
