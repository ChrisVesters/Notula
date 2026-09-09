Notula
=

What the product is, what it does today, and the constraints any implementation
of it has to satisfy. Written to be enough to rebuild the system from nothing.

`ROADMAP.md` says where it is going. `SEQUENCING.md` is a design for one part of
it. This document is the part that does not change when the implementation does.

The product
==

Notula is a shared agenda and note-taking tool for meetings. A team writes the
agenda before the meeting, takes notes into it together while the meeting
happens, and leaves with the notes already written.

The goal is to make the whole meeting cheaper, not just the note-taking: you
walk in with an agenda that is already timed, you leave with decisions and
action items captured, and the follow-up happens without anyone writing a
summary mail afterwards.

Two properties follow from that and drive most of the design:

- **It is used live, by several people, in the same room or call.** Notes are
  taken *during* the meeting, by whoever is talking least. Everyone has it open.
  This is not a document that one person edits and others read later.
- **The agenda is the structure.** Notes are not a flat page; they hang off the
  topic being discussed. That is what makes them useful afterwards, and what
  lets a topic carry its own timing.

Who uses it
==

- **An organisation** is the tenant. Everything — meetings, users, notes —
  belongs to exactly one.
- **A member** is a user in an organisation. Members create meetings, write
  agendas, and take notes.
- **An admin** is a member who can also manage the organisation and its people.

A user may belong to more than one organisation and picks which one they are
working in; the session carries that choice.

Today every member of an organisation can open every meeting in it. Per-meeting
access is a deliberate gap, not an oversight — see *Not decided yet*.

The core loop
==

1. Someone creates a meeting and gives it a name and description.
2. They add **topics** — the agenda. Each topic has a name, a description and
   optionally a planned duration in minutes. Topics are ordered.
3. During the meeting, participants open it and take notes. Notes are
   **blocks** that hang off a topic, ordered within it. Today the only kind of
   block is text.
4. Everyone sees everyone else's edits as they are made.

That is the whole of what exists today. Everything else in `ROADMAP.md` —
action items, decisions, meeting times, attendees, export, timers — extends this
loop rather than replacing it.

Domain model
==

The nouns, and what is actually true of them today.

| Entity | Belongs to | Carries |
| --- | --- | --- |
| Organisation | — | name |
| User | — | email (unique) |
| Credential | user | password hash (Argon2) |
| Organisation membership | organisation + user | role: `ADMIN` or `MEMBER` |
| Session | user, organisation | refresh token, active until |
| Meeting | organisation | name, description |
| Topic | meeting | order, name, description, duration (minutes, optional) |
| Block | topic | order, type (`TEXT` only) |
| Text block | block | content |

Relationships are strict containment: an organisation has meetings, a meeting
has topics, a topic has blocks, a block has its typed content. Every row below
the organisation also carries `organisation_id` directly, so authorisation never
needs to walk up the tree.

A block's content lives in a per-type table keyed by block id. `BlockType` has
one value today, but the split exists so that action items and decisions can be
added as further types without touching `blocks`.

Notably **absent** today, and load-bearing for later work: a meeting has no date
or time, no attendees, and nothing records who wrote what.

What real-time has to guarantee
==

This is the hard part of the product and the reason the system is not just CRUD.
Several people edit one meeting at once, so an implementation has to answer all
of the following. These are requirements, not a design.

**Everyone converges.** Two people editing at the same time must end up seeing
the same thing. For structure — creating, moving, deleting topics and blocks —
that means every client applies changes in one agreed order. For text it means
more: two edits to the same paragraph have to merge, not overwrite.

**Text edits are edits, not snapshots.** A change to a text block is a splice:
*replace `length` characters at `position` with `value`*. It is never the whole
new document. The same shape applies to a topic's or meeting's name and
description. This matters because it is what makes merging possible at all, and
because it means an edit is only meaningful against the state it was composed
against — that base has to travel with it.

**A client must be able to tell it has fallen behind.** Connections drop,
tokens expire, laptops close. A client that misses a change must be able to
notice and recover, rather than carrying on with a view that has quietly
diverged from everyone else's.

**A client must learn what became of what it sent.** Whether it applied, where
it landed in the order, and if it failed, whether sending it again could work.

**Ordering is per meeting.** Two meetings never interact. Whatever provides
order and consistency is naturally scoped to one meeting, and that is also the
unit people subscribe to.

**Correctness cannot depend on client behaviour.** A client that misbehaves may
lose its own work; it must not corrupt the meeting for everybody else.

Authentication and tenancy
==

- Password login issues a short-lived **access token** (JWT, 30 minutes) and a
  **refresh token** held in a cookie. The session stays refreshable for 7 days.
- The client refreshes the access token on a timer before it expires.
- Every request and every live connection is authenticated by the access token,
  and authorisation is scoped to the organisation on the session.
- A live connection authenticated with a token must not outlive that token's
  validity, or a user keeps rights that have since been taken away.

Current interfaces
==

Recorded as reference, not as a constraint on a rewrite.

**REST** under `/api` for the things that are not live: `sessions` (login,
refresh, logout), `users`, `organisations`, `organisation-users`, and `meetings`
(list, create, delete).

**A live connection** for everything inside a meeting: subscribe to a meeting to
receive its current state and then a stream of changes; send changes to topics,
blocks, text blocks and the meeting itself.

**Screens**: register; select organisation; meetings list; the meeting page
(info, agenda, notes); organisation settings and users for admins.

Current stack
==

Java 25 / Spring Boot (webmvc, security with OAuth2 resource server, JPA,
WebSocket with STOMP), PostgreSQL with Flyway migrations, Lombok, Testcontainers
for tests. SvelteKit 5 with runes, `@stomp/stompjs`, `sveltekit-i18n` (English
only so far), Vite, Vitest with Playwright.

The test suite is thorough — over a thousand backend tests — and that
expectation should carry into anything that replaces it.

What we learned building the first version
==

Worth reading before designing the second. These were paid for.

**The granularity mismatch is the root problem.** The domain was modelled as
fine-grained RPC: every mutation got its own endpoint, request type, domain
action, event and publisher, for each of four entity types. But consistency and
concurrency are properties of a *meeting*. Every cross-cutting concern — who
made a change, which position it landed at, acknowledging it, ordering it — then
had to be threaded through all of those types. Adding one field to every event
touched about forty files. A design where changes to a meeting share one
envelope pays that once.

**Prefer one document-scoped change stream over per-entity endpoints.** One
inbound message ("a change to meeting X, composed against version V"), one
outbound message ("change applied at version R by client C"). Structure changes
and text changes are the same kind of thing at that level.

**Serialise per meeting, and let that be the only ordering mechanism.** A single
consumer per meeting makes mutual exclusion and ordering the same thing, and
both free. The first version reached for a per-meeting lock, a per-session
ordering gate and a dedicated thread pool to approximate this, because the
framework's request-shaped machinery dispatched concurrently underneath.

**Broadcast after the write commits, never during.** Otherwise a client can be
told about a change it cannot yet read.

**Do not denormalise `meeting_id` onto blocks.** It was tried and removed. If an
implementation finds itself needing it, that is a signal the change stream is
not scoped to a meeting.

**Framework friction is real and worth measuring early.** In the first version:
the inbound message channel silently shared a two-thread scheduler with
heartbeats; handler exceptions were swallowed by the framework and never reached
the surrounding machinery; and asking for the outbound messaging template inside
a channel interceptor is a dependency cycle. None of these are inherent to live
collaboration — they came from using a request/response abstraction for a
stream.

**Keep the total order visible in the data.** A per-meeting revision that
advances with every change, written in the same transaction as the change, gives
gap detection, acknowledgement and a base for merging, and is the only mechanism
that still works if more than one server instance is involved.

Not decided yet
==

Open questions a rewrite should answer deliberately rather than inherit.

- **How text merges.** Operational transformation against a per-block version,
  or a CRDT. The first version chose OT on the grounds that a CRDT turns the
  server into a relay around an opaque binary document, which fights wanting to
  search, export and attribute the text. That reasoning still holds, but the
  decision is open; a CRDT wins outright if offline editing becomes a
  requirement.
- **How long a history of changes is kept.** Rebasing needs only back to the
  oldest version any live client holds, but undo/redo and authorship
  attribution want it kept.
- **What happens to changes queued behind one that failed.** They were composed
  against a state the server never reached.
- **Whether more than one server instance is supported**, which decides whether
  in-process serialisation is sufficient.
- **Per-meeting access.** Today organisation membership grants access to every
  meeting. Attendees make this meaningful.
