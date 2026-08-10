Roadmap
=

This is a proposed direction for Notula, not a commitment. Phases are ordered by
dependency and value: each one leans on what the previous one put in place.

Sizes are rough: **S** = a day or two, **M** = about a week, **L** = multiple
weeks or a design decision first.

Where an item maps onto something that already exists in the code, the seam is
named — several of these are extensions of structures that are already in place
rather than new subsystems.

Product goal
==

Notula should make the whole meeting cheaper, not just the note-taking: you walk
in with an agenda that is already timed, you leave with decisions and action
items captured, and the follow-up happens without anyone writing a summary
mail. Everything below serves that loop.

Known risks and open decisions
==

These want an answer before the phases they sit in.

- **Concurrent edits to the same text can corrupt each other.**
  `common/domain/TextUpdate` applies an incoming edit as a raw
  `position`/`length` splice against current server state. Two people typing in
  the same text block at the same time will apply each other's offsets against
  text that has already shifted. This is the headline feature of the product, so
  it needs a real answer: operational transformation, or a CRDT, or a
  server-assigned revision number that clients rebase against. Scheduled in
  Phase 3, but it should move up the moment more than one person uses a meeting
  at the same time in anger.
- **Reordering events are not broadcast.** `TopicService` and `BlockService`
  shift `sequence_id` on neighbouring rows when something is created or
  deleted, but the `// TODO: publish move action/event!!` means other connected
  clients never hear about the shift. Their view silently drifts out of order
  until they reload. Treated as a bug in Phase 1.
- **A user added to an organisation cannot log in.**
  `OrganisationUserService.create` creates a `UserInfo` for an unknown email
  with no credential row, so there is no password and no way to set one. Also
  Phase 1.
- **No meeting has a time.** The `meetings` table holds a name and a
  description. Almost everything in Phases 2 and 4 — carry-over, recurrence,
  calendar sync, upcoming-vs-past — needs this first.

Phase 1 — A meeting is a real event
==

Closes the functional dead ends and gives a meeting the fields the rest of the
roadmap depends on.

- **Invitation and credential flow** (L) — invite token, invitation mail,
  set-password on accept. Fixes the dead end in `OrganisationUserService`.
  Touches `credentials`, a new invitations table, and needs mail delivery, which
  nothing in the project does yet.
- **Password reset and email verification** (M) — rides on the same mail
  delivery, so worth doing directly after the invitation flow.
- **Meeting date, time and planned duration** (S) — new columns on `meetings`,
  a Flyway migration, and upcoming-vs-past splitting on the meetings list.
- **Attendees** (M) — invite specific organisation members to a meeting and
  record who attended. Today any member of the organisation can open any
  meeting; this is the point where per-meeting access becomes meaningful.
- **Broadcast move events** (M) — publish a move action from `TopicService` and
  `BlockService` when sequence IDs shift, and apply it in the meeting page's
  event handler. Bug fix, and the precondition for the next item.
- **Drag-and-drop reordering** (M) — of topics and of blocks, on top of the move
  event above.
- **Live topic timer** (S) — a running clock per topic against its `duration`,
  with an overrun indicator and the agenda total against the meeting's planned
  length. The duration data already exists and nothing consumes it yet, so this
  is mostly frontend and the cheapest visible win on the list.

Phase 2 — Meetings produce outcomes
==

The point where notes stop being a transcript and start being something you act
on. `BlockType` currently has a single value, `TEXT`, and `BlockContent` is
already a sealed interface with a per-type table — the whole phase is built in
that seam.

- **`ACTION_ITEM` block type** (L) — assignee, due date, done flag. The first
  non-text block, so it also proves the block model generalises.
- **Personal action item view** (M) — everything assigned to me across every
  meeting, with overdue highlighting. The `dashboard` translation key and
  `IconDashboard` exist but there is no route behind them yet.
- **`DECISION` block type** (M) — and an organisation-wide decision log, so
  teams stop re-litigating things nobody can find.
- **Meeting states** (M) — draft, in progress, closed. Closing freezes the notes
  into actual minutes instead of a document that stays editable forever.
- **Carry-over** (M) — unfinished topics and open action items roll into the
  next meeting in the series. Needs Phase 4's series concept to be fully useful,
  but works between any two meetings first.
- **Export and send minutes** (M) — Markdown and PDF export, and mail the
  minutes to attendees when a meeting closes.
- **More block types** (S each) — heading, list, checklist. Cheap once the first
  non-text type exists.

Phase 3 — Real-time that holds up
==

Phase 1 and 2 add data; this phase makes multi-person editing trustworthy.

- **Conflict-safe text editing** (L) — see the risk above. Design decision
  first, then implementation. Everything else in this phase is easier once
  edits carry a revision.
- **Presence** (M) — who is in the meeting right now, as avatars.
  `DetailsWebSocket` already has the subscribe hook to hang this on.
- **Authorship attribution** (M) — who wrote which note. Nothing in `blocks` or
  `text_blocks` records an author today, so this is a schema change.
- **Live cursors and per-block typing indicators** (M) — natural once presence
  and revisions exist.
- **Shared current-topic pointer** (S) — highlight the topic under discussion
  for everyone, over the existing WebSocket. Pairs with the Phase 1 timer.
- **Comments and @mentions** (L) — threaded follow-up on a topic or block.
  Mentions pair with action items and want notifications behind them.

Phase 4 — Fits into how people already work
==

- **Full-text search** (M) — across meetings, topics and notes. Postgres does
  this without new infrastructure.
- **Agenda templates** (S) — save a topic structure, start new meetings from it.
- **Recurring meetings and series** (M) — a weekly standup carries its agenda
  skeleton and its carry-over forward, and you can walk back through previous
  occurrences.
- **Calendar sync** (L) — Google, Outlook, ICS. Pulls the time and the invitee
  list, which is what makes the tool part of the routine rather than another
  place to remember to open.
- **Chat integrations** (M) — post minutes and action items to Slack or Teams on
  close.
- **Issue tracker integration** (M) — push an action item to Jira or GitHub.
- **Read-only share links** (M) — minutes for people outside the organisation.
- **Public API and webhooks** (L).

Ongoing
==

Not phased — picked up alongside the work above.

Editing experience
===

- Undo and redo. Both `Input.svelte` and `TextArea.svelte` flag it.
- Keyboard-first note-taking: Enter for a new block, Tab to indent, arrows
  between blocks. The largest speed win for the core interaction.
- Rich text — bold, italic, links. The editors are plain `input` and `textarea`
  today.
- Markdown and slash-command shortcuts for creating blocks.

Robustness
===

- Real error handling in the frontend. `window.alert` is currently used for
  login failure, meeting creation, deletion and WebSocket errors.
- Let the client identify which request failed —
  `WebSocketExceptionHandler` notes both gaps.
- Reconnect and out-of-order resilience: the meeting page has open TODOs about
  events arriving before the initial load, and about echoing your own events
  back to yourself; `WebSocketClient` has an open question about what happens
  when the access token expires mid-connection.
- Tighten `HttpExceptionHandler` mappings.

Accounts and administration
===

- User profiles — display name and avatar. Users are currently only an email,
  which makes attribution and presence look thin.
- Change email and change password.
- More roles than `ADMIN` and `MEMBER`: viewer or guest, and per-meeting
  permissions.
- SSO. `spring-boot-starter-security-oauth2-resource-server` is already a
  dependency.
- Audit log of administrative actions.

Presentation
===

- Mobile layout.
- Dark mode.
- More languages. The `sveltekit-i18n` scaffolding is in place with only `en`
  populated.

If you only do four things
==

**Meeting date and attendees**, because they unblock a whole tier of later work.
**Action items as a block type**, the highest user value and a clean fit for the
existing model. **The invitation and password flow**, because adding a colleague
to an organisation currently produces an account that cannot log in. **The live
topic timer**, because the data is already stored and nothing is using it.
