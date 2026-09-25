package com.cvesters.notula.meeting;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.test.context.jdbc.Sql;

import com.cvesters.notula.session.TestSession;
import com.cvesters.notula.test.FrameHandler;
import com.cvesters.notula.test.WebSocketTest;
import com.cvesters.notula.topic.TestTopic;

// No service is mocked here, deliberately. The lock, the transaction and the
// publisher only order events with respect to one another when they run
// together, so a @MockitoBean on any of them removes what this asserts while
// leaving it green.
@Sql({ "/db/clean.sql", "/db/users.sql", "/db/organisations.sql",
		"/db/meetings.sql", "/db/topics.sql" })
public class MeetingChangeWebSocketTest extends WebSocketTest {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
	private static final TestMeeting MEETING = TestMeeting.SPORER_PROJECT;

	private static final TestTopic DELIVERABLES = TestTopic.SPORER_PROJECT_DELIVERABLES;
	private static final TestTopic BLOCKERS = TestTopic.SPORER_PROJECT_BLOCKERS;
	private static final TestTopic TIMELINE = TestTopic.SPORER_PROJECT_TIMELINE;

	private static final String CHANGES = "/app/meetings/" + MEETING.getId()
			+ "/changes";
	private static final String EVENTS = "/topic/meetings/" + MEETING.getId();
	private static final String SNAPSHOT = "/app/meetings/" + MEETING.getId();

	private static final UUID CHANGE_ID = UUID
			.fromString("6f2a9c18-3b5d-4e07-9a61-8c4d2e0b7f35");

	private static final UUID SECOND_CHANGE_ID = UUID
			.fromString("2d8e4f60-9a1b-4c37-b5e2-6f0a3d9c1b84");

	private static final UUID REFUSED_CHANGE_ID = UUID
			.fromString("b0d7e6a4-1c39-4f52-8e7b-2a5f9c3d04e8");

	private static final long UNKNOWN_TOPIC_ID = 9999L;

	private static final long REVISION = MEETING.getRevision() + 1;

	private static final Duration EVENT_TIMEOUT = Duration.ofSeconds(5);

	@Nested
	class Broadcast {

		@Test
		void move() throws Exception {
			final FrameHandler events = observing();
			final StompSession author = connect(SESSION);

			send(author, CHANGES, CHANGE_ID, payload("""
					{
						"type": "MOVE_TOPIC",
						"topic": %d,
						"afterId": null
					}
					""".formatted(TIMELINE.getId())));

			final List<String> received = events.await(1, EVENT_TIMEOUT);

			assertThat(received).hasSize(1);
			assertThat(received.getFirst()).isEqualToIgnoringWhitespace(
					event(REVISION, moveMutation(TIMELINE.getId(), "0V")));
		}

		@Test
		void unchanged() throws Exception {
			final FrameHandler events = observing();
			final StompSession author = connect(SESSION);

			send(author, CHANGES, CHANGE_ID, payload("""
					{
						"type": "MOVE_TOPIC",
						"topic": %d,
						"afterId": %d
					}
					""".formatted(TIMELINE.getId(), BLOCKERS.getId())));
			send(author, CHANGES, SECOND_CHANGE_ID, payload("""
					{
						"type": "SCHEDULE_TOPIC",
						"topic": %d,
						"minutes": 5
					}
					""".formatted(DELIVERABLES.getId())));

			final List<String> received = events.await(2, EVENT_TIMEOUT);

			assertThat(received).hasSize(2);
			assertThat(received.get(0)).isEqualToIgnoringWhitespace(
					event(REVISION, moveMutation(TIMELINE.getId(),
							TIMELINE.getRank().value())));
			assertThat(received.get(1)).isEqualToIgnoringWhitespace(event(
					REVISION + 1, scheduleMutation(DELIVERABLES.getId(), 5)));
		}

		@Test
		void consecutive() throws Exception {
			final FrameHandler events = observing();
			final StompSession author = connect(SESSION);

			send(author, CHANGES, CHANGE_ID, payload("""
					{
						"type": "MOVE_TOPIC",
						"topic": %d,
						"afterId": null
					}
					""".formatted(TIMELINE.getId())));
			send(author, CHANGES, SECOND_CHANGE_ID, payload("""
					{
						"type": "SCHEDULE_TOPIC",
						"topic": %d,
						"minutes": 5
					}
					""".formatted(DELIVERABLES.getId())));

			final List<String> received = events.await(2, EVENT_TIMEOUT);

			assertThat(received).hasSize(2);
			assertThat(received.get(0)).isEqualToIgnoringWhitespace(
					event(REVISION, moveMutation(TIMELINE.getId(), "0V")));
			assertThat(received.get(1)).isEqualToIgnoringWhitespace(event(
					REVISION + 1, scheduleMutation(DELIVERABLES.getId(), 5)));
		}
	}

	@Nested
	class Rebase {

		@Test
		void concurrent() throws Exception {
			final FrameHandler events = observing();
			final StompSession author = connect(SESSION);
			final long base = MEETING.getRevision();

			send(author, CHANGES, CHANGE_ID, payload("""
					{
						"type": "RENAME_TOPIC",
						"topic": %d,
						"base": %d,
						"position": 0,
						"length": 0,
						"value": "Q3 "
					}
					""".formatted(TIMELINE.getId(), base)));
			send(author, CHANGES, SECOND_CHANGE_ID, payload("""
					{
						"type": "RENAME_TOPIC",
						"topic": %d,
						"base": %d,
						"position": 8,
						"length": 0,
						"value": " plan"
					}
					""".formatted(TIMELINE.getId(), base)));

			final List<String> received = events.await(2, EVENT_TIMEOUT);

			assertThat(received).hasSize(2);
			assertThat(received.get(0)).isEqualToIgnoringWhitespace(
					event(REVISION, """
							{
								"type": "RENAME_TOPIC",
								"topic": %d,
								"position": 0,
								"length": 0,
								"value": "Q3 "
							}
							""".formatted(TIMELINE.getId())));
			assertThat(received.get(1)).isEqualToIgnoringWhitespace(
					event(REVISION + 1, """
							{
								"type": "RENAME_TOPIC",
								"topic": %d,
								"position": 11,
								"length": 0,
								"value": " plan"
							}
							""".formatted(TIMELINE.getId())));

			final String name = jdbcTemplate.queryForObject(
					"SELECT name FROM topics WHERE id = ?", String.class,
					TIMELINE.getId());
			assertThat(name).isEqualTo("Q3 Timeline plan");
		}
	}

	@Nested
	class Replay {

		@Test
		void afterChange() throws Exception {
			final FrameHandler events = observing();
			final StompSession author = connect(SESSION);

			send(author, CHANGES, CHANGE_ID, payload("""
					{
						"type": "MOVE_TOPIC",
						"topic": %d,
						"afterId": null
					}
					""".formatted(TIMELINE.getId())));
			send(author, CHANGES, SECOND_CHANGE_ID, payload("""
					{
						"type": "SCHEDULE_TOPIC",
						"topic": %d,
						"minutes": 5
					}
					""".formatted(DELIVERABLES.getId())));
			final List<String> received = events.await(2, EVENT_TIMEOUT);
			assertThat(received).hasSize(2);

			final FrameHandler replay = subscribe(author,
					"/app/meetings/" + MEETING.getId() + "/events/" + REVISION);

			assertThat(replay.getResponse()).succeedsWithin(EVENT_TIMEOUT)
					.satisfies(replayed -> assertThat(replayed)
							.isEqualToIgnoringWhitespace(
									"[" + received.get(1) + "]"));
		}

		@Test
		void otherOrganisation() throws Exception {
			final StompSession author = connect(SESSION);
			final FrameHandler rejections = subscribeToRejections(author);

			subscribe(author, "/app/meetings/"
					+ TestMeeting.GLOVER_KICKOFF_2026.getId() + "/events/0");

			assertThat(rejections.getResponse()).succeedsWithin(EVENT_TIMEOUT)
					.satisfies(rejected -> assertThat(rejected)
							.contains("\"retryable\":false"));
		}
	}

	@Nested
	class Resend {

		@Test
		void logged() throws Exception {
			final FrameHandler events = observing();
			final StompSession author = connect(SESSION);
			final FrameHandler acks = subscribeToAcknowledgements(author);
			final FrameHandler snapshot = subscribe(author, SNAPSHOT);
			assertThat(snapshot.getResponse()).succeedsWithin(EVENT_TIMEOUT);
			final String rename = """
					{
						"type": "RENAME_TOPIC",
						"topic": %d,
						"base": %d,
						"position": 0,
						"length": 0,
						"value": "Q3 "
					}
					""".formatted(TIMELINE.getId(), MEETING.getRevision());

			send(author, CHANGES, CHANGE_ID, payload(rename));
			send(author, CHANGES, CHANGE_ID, payload(rename));

			final List<String> acknowledged = acks.await(2, EVENT_TIMEOUT);
			assertThat(acknowledged).hasSize(2);
			assertThat(acknowledged.get(0)).isEqualToIgnoringWhitespace(
					acknowledged(CHANGE_ID, REVISION));
			assertThat(acknowledged.get(1)).isEqualToIgnoringWhitespace(
					acknowledged(CHANGE_ID, REVISION));

			final List<String> received = events.await(2, EVENT_TIMEOUT);
			assertThat(received).hasSize(1);

			final String name = jdbcTemplate.queryForObject(
					"SELECT name FROM topics WHERE id = ?", String.class,
					TIMELINE.getId());
			assertThat(name).isEqualTo("Q3 Timeline");
		}
	}

	@Nested
	class Acknowledge {

		@Test
		void change() throws Exception {
			final StompSession author = connect(SESSION);
			final FrameHandler acks = acknowledging(author);

			send(author, CHANGES, CHANGE_ID, payload("""
					{
						"type": "SCHEDULE_TOPIC",
						"topic": %d,
						"minutes": 5
					}
					""".formatted(DELIVERABLES.getId())));

			final List<String> received = acks.await(1, EVENT_TIMEOUT);

			assertThat(received).hasSize(1);
			assertThat(received.get(0)).isEqualToIgnoringWhitespace(
					acknowledged(CHANGE_ID, REVISION));
		}

		// The refused change is sent first and one session's frames are
		// handled in turn, so an acknowledgement for it would arrive ahead of
		// the one asserted on rather than racing it. Its revision would too:
		// the bump rolls back with the change, which is why the change that
		// follows commits at the same number the refused one reached for.
		@Test
		void refused() throws Exception {
			final StompSession author = connect(SESSION);
			final FrameHandler acks = acknowledging(author);

			send(author, CHANGES, REFUSED_CHANGE_ID, payload("""
					{
						"type": "SCHEDULE_TOPIC",
						"topic": %d,
						"minutes": 5
					}
					""".formatted(UNKNOWN_TOPIC_ID)));
			send(author, CHANGES, CHANGE_ID, payload("""
					{
						"type": "SCHEDULE_TOPIC",
						"topic": %d,
						"minutes": 5
					}
					""".formatted(DELIVERABLES.getId())));

			final List<String> received = acks.await(1, EVENT_TIMEOUT);

			assertThat(received).hasSize(1);
			assertThat(received.get(0)).isEqualToIgnoringWhitespace(
					acknowledged(CHANGE_ID, REVISION));
		}

		// Subscribing and sending on one connection is ordered, but the
		// subscription still has to reach the broker before the change is
		// handled, and only the round trip proves that it has.
		private FrameHandler acknowledging(final StompSession session)
				throws Exception {
			final FrameHandler acks = subscribeToAcknowledgements(session);

			final FrameHandler snapshot = subscribe(session, SNAPSHOT);
			assertThat(snapshot.getResponse()).succeedsWithin(EVENT_TIMEOUT);

			return acks;
		}
	}

	// A second connection has to be watching before the change is sent, and
	// nothing orders frames across two connections. The simple broker sends no
	// receipt for a SUBSCRIBE, so there is no barrier to wait on directly;
	// asking the same session for the snapshot and waiting for the reply is a
	// full round trip through the application, which the subscription queued
	// ahead of it cannot still be behind.
	@Nested
	class Log {

		@Test
		void change() throws Exception {
			final FrameHandler events = observing();
			final StompSession author = connect(SESSION);

			send(author, CHANGES, CHANGE_ID, payload("""
					{
						"type": "MOVE_TOPIC",
						"topic": %d,
						"afterId": null
					}
					""".formatted(TIMELINE.getId())));

			final List<String> received = events.await(1, EVENT_TIMEOUT);
			assertThat(received).hasSize(1);

			final Integer logged = jdbcTemplate.queryForObject("""
					SELECT count(*) FROM events
					WHERE meeting_id = ? AND revision = ? AND user_id = ?
						AND client_id = ? AND mutation = ?::jsonb
					""", Integer.class, MEETING.getId(), REVISION,
					SESSION.getUser().getId(), CLIENT_ID,
					moveMutation(TIMELINE.getId(), "0V"));
			assertThat(logged).isEqualTo(1);
		}
	}

	private FrameHandler observing() throws Exception {
		final StompSession observer = connect(SESSION);
		final FrameHandler events = subscribe(observer, EVENTS);

		final FrameHandler snapshot = subscribe(observer, SNAPSHOT);
		assertThat(snapshot.getResponse()).succeedsWithin(EVENT_TIMEOUT);

		return events;
	}

	private static byte[] payload(final String change) {
		return change.getBytes(StandardCharsets.UTF_8);
	}

	private static String event(final long revision, final String mutation) {
		return """
				{
					"revision": %d,
					"origin": {
						"userId": %d,
						"clientId": "%s"
					},
					"mutation": %s
				}
				""".formatted(revision, SESSION.getUser().getId(), CLIENT_ID,
				mutation);
	}

	private static String acknowledged(final UUID changeId,
			final long revision) {
		return """
				{
					"id": "%s",
					"revision": %d
				}
				""".formatted(changeId, revision);
	}

	private static String moveMutation(final long topicId, final String rank) {
		return """
				{
					"type": "MOVE_TOPIC",
					"topic": %d,
					"rank": "%s"
				}
				""".formatted(topicId, rank);
	}

	private static String scheduleMutation(final long topicId,
			final int minutes) {
		return """
				{
					"type": "SCHEDULE_TOPIC",
					"topic": %d,
					"minutes": %d
				}
				""".formatted(topicId, minutes);
	}
}
