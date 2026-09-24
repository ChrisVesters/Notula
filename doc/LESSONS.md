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

Never leave a failing test in the shared tree
==

Confirming a suspected bug and probing a framework's behaviour are both best
done by writing a test that fails. Twice that test was written straight into
`MeetingChangeWebSocketTest` and removed a few minutes later: once to show that
a no-op move spends a revision, once to find out whether the simple broker
answers a `SUBSCRIBE` with a `RECEIPT`. Both failed exactly as intended.

In between, the person working alongside ran the suite, saw a red test with a
name they had just been told was green, and reported it. Reproducing a failure
that was never in the code cost more than the two probes had saved, and it
spends the only thing a report like that relies on — that "the suite is green"
means something.

**A probe belongs in a scratch file outside the project, not in the class it is
probing.** `mvn test -Dtest=Foo#bar` runs one method from anywhere. And a test
filter that matches nothing still prints `Tests run: 0` and exits zero, so a
green run of a probe is not evidence the probe ran.

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

Step 6, the first time
==

Concurrent text editing (`SEQUENCING.md`, step 6) was built end to end and
worked. It was built as a `base-revision` header, an `events` log, a `Splice`
transform on both sides, rebasing in `ChangeService`, and a client that owns
the stream and rebases its queue. `MeetingChangeWebSocketTest` drove two
concurrent renames through the real path, and a second subscriber saw them
merged. The design survives, and `SEQUENCING.md` now holds it with the order to
rebuild it in. The work itself was stashed, not committed, because of how it
was done.

It was done in one piece. About sixty files across backend, frontend and four
design documents stayed uncommitted while the work grew. Then a refactor went on
top: an `Event` BDO for the log's storage gateway, which touched another
forty-odd files, including a package rename. Every step was green. None of it
could be reviewed, because there was nothing between "nothing" and
"everything".

It also could not be taken apart. Much of the work was untracked, so git held
no copy of it, and the refactor could not be undone with git either. Every file
had to be put back by hand from the originals that had been read. One deleted
test with local modifications was lost outright: `git rm` refused it, the refusal
was worked around with a plain `rm`, and only the committed version could be
restored. Reverting cost more than writing.

**Build a large change as small green steps, and commit each one.** A step is
small enough to review in one sitting. It compiles, it passes
`mvn clean test`, and it moves the design doc along with the code. A refactor
proposed during a feature waits until the feature's steps are committed. Two
changes interleaved in one uncommitted tree can only be kept or discarded
together.

**Never delete a file that has local modifications.** When git refuses a
`git rm`, that refusal is the warning, not an obstacle to route around.

**A design doc's objection can rest on an assumption the design has since
dropped.** Step 6 used to say the version had to be per block, because a
meeting-wide number would manufacture conflicts between blocks. That was true
of a compare-and-set, which refuses. An edit that is rebased is only ever moved
over edits to its own text, so the objection disappears, and so does the
per-block counter. Re-read the reasons, not just the conclusions, when the
mechanism changes.

A timeout that logs is a timeout that lies
==

`SessionOrder.acquire` waited on a per-session semaphore. When the wait ran out
it logged a warning and returned, exactly as though it had the permit. Two
things followed, and the second is the expensive one.

The frame ran unordered, which is the guarantee the class exists to provide. And
the interceptor afterwards called `release`, because from where it stood the
frame had been handled normally. A semaphore hands out a permit on every
release without checking who held one, so the session went from one permit to
two, and from then on its frames ran two at a time. One slow change disabled
ordering for the rest of that connection, silently and permanently.

None of this showed up. `SessionOrderTest` covered acquire, release, forget,
multiple actions and multiple sessions. It had no case for the timeout, and one
test asserted the broken shape directly: acquire twice on an impatient lock,
release once, expect the next acquire to succeed. It did succeed. That is the
leak, written down as the expected result.

What it cost to find: nothing, once someone ran it. A twenty-line probe against
the real class showed two threads inside the critical section at once. Reading
the method had not shown it, because the method looks fine in isolation — the
bug lives in the pair, between a caller that did not acquire and a caller that
released anyway.

- **Test the timeout branch of anything that has one.** It is the branch that
  runs when the system is already in trouble, which is the worst time to
  discover it is wrong.
- **A method that can fail must say so** — or be given no way to fail. `acquire`
  returned a boolean for a while, so the caller released only what it took. It
  is `void` again because the wait is now unbounded and taking the turn cannot
  fail, and `Turn` hands a permit back at most once per turn rather than
  trusting the caller.
- **Watch for tests that encode the bug.** `multipleAquires` passed for the
  whole life of the defect. A test that documents current behaviour rather than
  intended behaviour will defend a bug as loyally as it defends a feature.
- **The gate should not be refusing at all.** A frame dropped in `preSend` never
  reaches a handler, so `@MessageExceptionHandler` cannot report it and the
  interceptor has to answer the client itself. It cannot: `SimpMessagingTemplate`
  needs `clientInboundChannel`, which asks the configurers for their
  interceptors, which builds `WebSocketConfig`, which needs this interceptor.
  Confirmed by deleting the `@Lazy` and reading the failure,
  `brokerMessagingTemplate: Requested bean is currently in creation`. Two ways
  round it were written and thrown away, a collaborator holding the template and
  an application event with a listener; the first only moves the proxy and the
  second only hides the edge. The bounded wait existed to survive a permit that
  was never handed back, so closing that leak removes the refusal instead of
  rehoming it. `ChannelInterceptorChain.applyPreSend` triggers
  `afterSendCompletion` on every interceptor that already ran when a later one
  returns null, which is the missing hand-back.
- **Do not throw out of `preSend` instead.** It is the obvious tidier-looking
  move — let `acquire` throw and let the advice answer — and it disconnects the
  client. `preSend` runs inside `clientInboundChannel.send`, and
  `StompSubProtocolHandler.handleMessageFromClient` catches everything that
  escapes that call and routes it to `handleError`, which writes a STOMP ERROR
  frame and then `session.close(PROTOCOL_ERROR)`. One slow change would take the
  whole tab's connection down. The acquire cannot move into the handler either,
  where the advice would reach it: `preSend` is the last point that still sees a
  session's frames in arrival order, which is the whole reason the gate lives
  there.
