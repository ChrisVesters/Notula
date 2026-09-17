Roadmap
=

This is a proposed direction for Notula, not a commitment. Phases are ordered
by dependency and value: each one leans on what the previous one put in place.

Sizes are rough: **S** = a day or two, **M** = about a week, **L** = multiple
weeks or a design decision first.

Where an item maps onto something that already exists in the code, the seam is
named — several of these are extensions of structures that are already in
place rather than new subsystems. Where an item has since been built, it is
not listed any more; *Recently landed* says what closed, so the same gaps are
not reported twice.

Product goal
==

Notula should make the whole meeting cheaper, not just the note-taking: you
walk in with an agenda that is already timed, you leave with decisions and
action items captured, and the follow-up happens without anyone writing a
summary mail. Everything below serves that loop.

Recently landed
==

Checked against the code, not against the commit messages.

- **One change envelope.** `/app/meetings/{id}/changes` is the only inbound
  destination; `meeting/dto/ChangeDto` is a sealed interface over meeting, topic
  and block operations. Adding an operation is a record and a `@Type` entry.
- **Ordering.** `config/SessionOrder` serialises one tab's frames;
  `meeting/MeetingLock` serialises a meeting's changes and owns the
  transaction; `common/messaging/TransactionalPublisher` broadcasts after
  commit.
- **Rejections name the change.** `change-id` travels on the frame and comes
  back on `RejectedDto`, built with `RejectedDto.permanent` / `.temporary`.
- **Move events are broadcast.** `TopicService` and `BlockService` publish a
  move for every row whose `sequence_id` shifts, and the meeting page applies
  them. The old `// TODO: publish move action/event!!` is gone.
- **Drag-and-drop reordering.** `common/ReorderHandler` with `IconDrag` in
  `TopicAgendaView` and `BlockView`, sending `MOVE_TOPIC` / `MOVE_BLOCK`.
- **One event envelope.** Events leave as `meeting/dto/EventDto` — `origin`
  plus a sealed `MutationDto` on one `type` discriminator — mirroring
  `ChangeDto` and matched by `frontend/src/lib/meeting/event/EventTypes.ts`.
  A cross-cutting field on an event is now one record rather than a dozen
  files, which is what step 4 needs.
- **Remote edits are rendered.** The meeting page applies name, description
  and block-content edits from other people, and the editors keep the caret
  where the user put it rather than jumping to the end.
- **The socket survives a token refresh.** `WebSocketClient.reconnect` keeps
  the subscription map; `config/WebSocketSessionRegistry` closes a connection
  whose token has expired.
- **A move that changes nothing says so.** `TopicService.move` and
  `BlockService.move` publish the move for the entity at the position it
  already holds rather than returning silently, so the revision the change
  spent is one every client sees. Refusing it instead was the other candidate
  and was not taken: a request for a state that already holds is not an error,
  and a refusal now costs the sender its queued changes.
- **A change is acknowledged to its sender.** `meeting/ChangeService` is the
  boundary of a change: it takes the lock once, dispatches the sealed
  `ChangeDto` inside it and returns the `MeetingScope`, which
  `MeetingWebSocket` answers with on `/user/queue/acks`. The entity services
  lost their own `meetingLock.call` wrappers and take a `MeetingScope` instead.
  `MeetingWebSocketClient` holds one change in flight and queues the rest,
  releasing on the acknowledgement and dropping the queue on a refusal.
  `SEQUENCING.md` step 4 is closed; step 7 is what makes the queue merge
  rather than only wait.
- **A missed change is detected, not silently absorbed.** `meetings.revision`
  is bumped inside the change's own transaction by `meeting/MeetingLock`, rides
  out on `EventDto` and the `/app/meetings/{id}` snapshot, and the meeting page
  compares the two: a gap resyncs, an event that arrived before the snapshot is
  buffered and replayed, and a repeat is dropped. `SEQUENCING.md` step 4, less
  the acknowledgement.

Known risks and open decisions
==

These want an answer before the phases they sit in.

- **One change drives the whole path; the rest still agree by construction.**
  `MeetingChangeWebSocketTest` carries a topic move and a schedule from a STOMP
  frame through the services to a second subscriber with nothing mocked, which
  pins one revision per change and consecutive changes differing by one. Every
  other operation — creates, deletes, text edits, block moves, and anything on
  the meeting itself — is still covered only by tests that mock the services on
  one side and assert the type on the other.

- **Concurrent edits to the same text can corrupt each other.**
  `common/domain/TextUpdate` applies an incoming edit as a raw
  `position`/`length` splice against current server state, and nothing carries
  a version. Two people editing one block apply each other's offsets against
  text that has already shifted. Answer is operational transformation, a CRDT,
  or a server revision clients rebase against; `SEQUENCING.md` is the design.
  Scheduled in Phase 3, and it should move up the moment more than one person
  uses a meeting at the same time in anger.
- **A user added to an organisation cannot log in.**
  `OrganisationUserService.create` creates a `UserInfo` for an unknown email
  with no credential row, so there is no password and no way to set one.
- **No meeting has a time.** The `meetings` table holds a name and a
  description. Almost everything in Phases 2 and 4 — carry-over, recurrence,
  calendar sync, upcoming-vs-past — needs this first.
- **One instance only.** `MeetingLock` and `SessionOrder` are in-process maps,
  and the broker is the in-memory simple broker. A second instance would order
  a meeting's changes independently and broadcast to only the clients it
  holds. Nothing warns about this; it is a deployment constraint the code does
  not state. See *Runs in production*.
- **Every delete is permanent.** `V1__init.sql` cascades from organisation to
  meeting to topic to block. Deleting a topic during a meeting destroys the
  notes under it for everybody, immediately, with no undo. Trash arrives in
  Phase 2; until then the risk is worth knowing.
- **Nothing runs the tests but a person.** There is no `.github/workflows`.
  The backend suite is substantial; the frontend one is thin. The frontend
  suite has now broken twice on its own configuration — first a stale Vitest
  browser provider, then a config that pointed at `src/**` while the tests
  live in `tests/`, which collects nothing and still exits green. A gate that
  nobody runs is how that keeps happening.

Phase 1 — A meeting is a real event
==

Closes the functional dead ends and gives a meeting the fields the rest of the
roadmap depends on.

- **Invitation and credential flow** (L) — invite token, invitation mail,
  set-password on accept. Fixes the dead end in `OrganisationUserService`.
  Touches `credentials`, a new invitations table, and needs mail delivery,
  which nothing in the project does yet.
- **Password reset and email verification** (M) — rides on the same mail
  delivery, so worth doing directly after the invitation flow.
- **Meeting date, time and planned duration** (S) — new columns on `meetings`,
  a Flyway migration, and upcoming-vs-past splitting on the meetings list.
  Store the instant with its timezone; a meeting is attended from several of
  them.
- **Attendees** (M) — invite specific organisation members to a meeting and
  record who attended. Today any member of the organisation can open any
  meeting; this is the point where per-meeting access becomes meaningful.
- **Live topic timer** (S) — a running clock per topic against its `duration`,
  with an overrun indicator and the agenda total against the meeting's planned
  length. The duration data already exists and nothing consumes it yet, so
  this is the cheapest visible win on the list.

Phase 2 — Meetings produce outcomes
==

The point where notes stop being a transcript and start being something you
act on. `BlockType` currently has a single value, `TEXT`, and `BlockContent`
is already a sealed interface with a per-type table — most of this phase is
built in that seam.

- **`ACTION_ITEM` block type** (L) — assignee, due date, done flag. The first
  non-text block, so it also proves the block model generalises.
- **Personal action item view** (M) — everything assigned to me across every
  meeting, with overdue highlighting. The `dashboard` translation key and
  `IconDashboard` exist but there is no route behind them yet.
- **`DECISION` block type** (M) — and an organisation-wide decision log, so
  teams stop re-litigating things nobody can find.
- **Meeting states** (M) — draft, in progress, closed. Closing freezes the
  notes into actual minutes instead of a document that stays editable forever.
- **Carry-over** (M) — unfinished topics and open action items roll into the
  next meeting in the series. Needs Phase 4's series concept to be fully
  useful, but works between any two meetings first.
- **Export and send minutes** (M) — Markdown and PDF export, and mail the
  minutes to attendees when a meeting closes.
- **Trash and restore** (M) — soft-delete meetings, topics and blocks with a
  retention window, instead of cascading them out of existence. Touches every
  read path, so it is cheaper now than after two more block types exist.
- **Attachments and images** (L) — a `FILE` block type, upload, and a storage
  decision: object store or `bytea`. The first thing in the product that is
  not text in Postgres, which is why it is an L rather than another block
  type.
- **Poll and vote block** (M) — a `POLL` block where attendees vote in the
  meeting. Pairs with decisions: the vote is the record of how one was
  reached.
- **Reactions** (S) — an emoji reaction per block, as agreement signals that
  do not need a comment thread. Cheap once presence names people.
- **Meeting roles** (S) — facilitator, note-taker, timekeeper as attributes of
  an attendee. Drives who the timer nags and who the minutes are sent from.
- **More block types** (S each) — heading, list, checklist. Cheap once the
  first non-text type exists.

Phase 3 — Real-time that holds up
==

Phase 1 and 2 add data; this phase makes multi-person editing trustworthy.
`SEQUENCING.md` is the design and its step numbering is the order to build in.

- **Conflict-safe text editing** (L) — see the risk above. Design decision
  first, then implementation. Everything else in this phase is easier once
  edits carry a revision.
- **Ask for what was missed** (M) — detection has landed; recovery is still a
  full reload. A client that knows it skipped revisions should be able to ask
  for those changes rather than re-fetching the whole meeting, which is what
  makes a reconnect, a closed laptop or a redeploy cheap instead of merely
  correct. Wants the operation log from `SEQUENCING.md` step 6 to have anything
  to replay, so it is worth doing after it rather than before.
- **Reconnect and recover** (M) — a dropped connection currently stays
  dropped: `WebSocketClient` sets `reconnectDelay: 0`, overriding the library's
  default of five seconds, so nothing retries and the tab goes quiet instead of
  silently rebaselining over what the sender never managed to send. That is a
  holding position, not an answer. Recovery is: reconnect deliberately,
  re-establish the subscriptions, and decide what becomes of the change that
  was in flight and the queue behind it — replaying them wants step 6's base
  versions, discarding them wants *Sync status* to say so. Note that the
  deliberate reconnect on token refresh already goes through the same gap:
  `Session.start` calls `WebSocketClient.reconnect`, and an acknowledgement
  lost in that window leaves the tab's queue stuck until the page is reloaded.
  Do these two together.
- **Sync status** (M) — one place in the UI that says whether the meeting is
  in sync, syncing, or disconnected. Today that state exists and is never
  shown: `MeetingWebSocketClient` holds one change in flight and a queue behind
  it, and discards both on a refusal without telling anyone, while a lost
  connection leaves the tab quietly doing nothing at all. A note-taker learns
  that their edits are not being saved by noticing that nobody else's are
  arriving either. The indicator is the fix, not a dialog: `window.alert`
  would fire on every transient drop, for something stompjs recovers from in
  five seconds. Bigger than the notice, though: this is the surface that later
  carries "catching up" once step 6's base versions let the client replay what
  was outstanding instead of discarding it, so build it as connection state
  rather than as an error message. Pairs with presence and subsumes the
  discarded-changes half of *Real error handling in the frontend*.
- **Presence** (M) — who is in the meeting right now, as avatars.
  `DetailsWebSocket` already has the subscribe hook to hang this on.
- **Authorship attribution** (M) — who wrote which note. Nothing in `blocks`
  or `text_blocks` records an author today, so this is a schema change.
- **Live cursors and per-block typing indicators** (M) — natural once presence
  and revisions exist.
- **Shared current-topic pointer** (S) — highlight the topic under discussion
  for everyone, over the existing WebSocket. Pairs with the Phase 1 timer.
- **Meeting history and replay** (M) — once the operation log from
  `SEQUENCING.md` step 6 exists, "what did this meeting look like at 14:05"
  and a diff since I last looked are almost free. Also what undo/redo and
  attribution both want kept.
- **Comments and @mentions** (L) — threaded follow-up on a topic or block.
  Mentions pair with action items and want notifications behind them.
- **Offline editing** (L) — a decision, not a task. `SEQUENCING.md` argues for
  transformation over a CRDT and names offline as the case that reverses it.
  If taking notes on a train is a requirement, decide it before step 6 is
  written, not after.

Phase 4 — Fits into how people already work
==

- **Notifications** (M) — in-app and email: an action item assigned to you, a
  mention, a meeting starting, minutes published. Nothing in the product can
  reach a user who does not have it open. Depends on the mail delivery from
  Phase 1 and gives mentions and action items somewhere to land.
- **Full-text search** (M) — across meetings, topics and notes. Postgres does
  this without new infrastructure.
- **Agenda templates** (S) — save a topic structure, start new meetings from
  it.
- **Recurring meetings and series** (M) — a weekly standup carries its agenda
  skeleton and its carry-over forward, and you can walk back through previous
  occurrences.
- **Calendar sync** (L) — Google, Outlook, ICS. Pulls the time and the invitee
  list, which is what makes the tool part of the routine rather than another
  place to remember to open.
- **Chat integrations** (M) — post minutes and action items to Slack or Teams
  on close.
- **Issue tracker integration** (M) — push an action item to Jira or GitHub.
- **Read-only share links** (M) — minutes for people outside the organisation.
- **Import** (S) — paste a Markdown agenda, or take one from a calendar
  invitation's description, and get topics. The fastest path out of "we
  already keep our agenda somewhere else".
- **Presentation mode** (S) — the agenda, current topic and timer, large
  enough for a shared screen, with the note-taking chrome gone.
- **Cross-meeting references** (M) — link a topic to the topic it continues,
  and show backlinks. What makes a series navigable once there are fifty of
  them.
- **Public API and webhooks** (L).

Phase 5 — Assistance
==

Only worth starting after Phase 3: every item here is a function of text with
authorship and history behind it, and produces nonsense without them.

- **Transcript import** (L) — take the recording or transcript a call platform
  already produces and attach it to the meeting, so the notes and the record
  of what was said live together.
- **Draft the minutes** (L) — summarise a closed meeting's notes per topic,
  and propose action items and decisions the note-taker can accept or discard.
  Proposals, never silent writes: an action item nobody agreed to is worse
  than a missing one.
- **Agenda suggestions** (M) — from the previous occurrence's carry-over and
  the open action items in the series.

All three send a tenant's meeting content to whatever model runs them, so the
first decision is not which model but what leaves the organisation, whether it
can be turned off per organisation, and what the data is retained for.

Runs in production
==

Nothing here changes the product, and all of it is between the current state
and being able to run this for someone other than yourself.

- **Continuous integration** (S) — no `.github/workflows` exists. Build, test
  and lint both halves on every push. The single highest-value item in this
  section, and the cheapest.
- **Container image and deployment** (M) — running the app today means two
  dev servers and an nginx container assembled by hand from `README.md`.
- **Observability** (M) — no actuator, metrics or tracing dependency in
  `backend/pom.xml`. Health checks, a metric on `MeetingLock` wait time and
  rejection counts by reason, and structured logs with the meeting id on them.
  Lock contention is invisible today until a user reports "try again".
- **Horizontal scaling** (L) — the in-process lock and the simple broker mean
  one instance. Sticky sessions plus Postgres advisory locks, or an external
  broker relay and a shared ordering mechanism. `PRODUCT.md` lists this as
  undecided; it is decided by the first deployment that needs to restart
  without dropping a meeting.
- **Backups and a restore drill** (S) — a backup nobody has restored is a
  belief, not a backup.
- **Rate limiting and abuse protection** (M) — login attempts, registration,
  and a ceiling on inbound frames per session. `SessionOrder` bounds
  concurrency per tab but nothing bounds volume.
- **Data protection** (M) — export and delete a user's data, delete an
  organisation, and a retention policy for closed meetings. `UserController`
  only registers and `OrganisationController` has no delete, so today a user
  or tenant cannot be removed at all.

Ongoing
==

Not phased — picked up alongside the work above.

Editing experience
===

- Undo and redo. Both `Input.svelte` and `TextArea.svelte` flag it.
- Keyboard-first note-taking: Enter for a new block, Tab to indent, arrows
  between blocks. The largest speed win for the core interaction.
- Nested blocks. `blocks` is flat under a topic today, so indenting is a
  display trick until the model carries a parent.
- Rich text — bold, italic, links. The editors are plain `input` and
  `textarea` today.
- Markdown and slash-command shortcuts for creating blocks.

Robustness
===

- Real error handling in the frontend. `window.alert` is still how login,
  registration, organisation switching, meeting creation and deletion, and
  change rejections all report failure — eight call sites.
- Use the rejection the server already sends. `MeetingWebSocketClient` now
  knows which change a refusal names — it drops its queue on one — but the
  page is still told only a reason, and every `send` call site drops the id it
  gets back, so a refusal still cannot be undone or retried where it happened.
- Out-of-order resilience is handled for events, not for the rest of the page.
  Events now carry a revision the page checks, buffers against and resyncs on,
  but `REMOVE_MEETING` still navigates away with no message, and an unknown
  `blockType` only logs.

Accounts and administration
===

- User profiles — display name and avatar. Users are currently only an email,
  which makes attribution and presence look thin.
- Change email and change password.
- Session management — list my active sessions and revoke one.
  `SessionService` can delete a session but nothing lists them, so a session
  on a lost laptop lives for its seven days.
- Password policy. `common/domain/Password` requires eight characters and
  nothing else; no breach check, no lockout.
- More roles than `ADMIN` and `MEMBER`: viewer or guest, and per-meeting
  permissions.
- SSO. `spring-boot-starter-security-oauth2-resource-server` is already a
  dependency.
- Audit log of administrative actions.

Quality
===

- End-to-end coverage beyond the one operation `MeetingChangeWebSocketTest`
  carries. `WebSocketTest` can now open several sessions and `FrameHandler` can
  await a sequence of frames, so the cost of the next one is the fixtures, not
  the harness.
- Frontend tests. Seven files cover three form components, one client, one
  editor component, the text-edit splice and the change queue, against 1052
  backend tests.
- Mutation testing is configured (`org.pitest:pitest-maven`) but is not part
  of any routine.

Presentation
===

- Mobile layout.
- Dark mode.
- Accessibility: keyboard navigation, focus management and screen-reader
  labels, which the drag-and-drop reordering and the custom editors both need.
- A print stylesheet, so minutes print as minutes.
- More languages. The `sveltekit-i18n` scaffolding is in place with only `en`
  populated.

If you only do three things
==

**Meeting date and attendees**, because they unblock a whole tier of later
work. **The invitation and password flow**, because adding a colleague to an
organisation currently produces an account that cannot log in. **Action items
as a block type**, the highest user value and a clean fit for the existing
model.

The live topic timer is the fourth, and still the cheapest thing on the list.
