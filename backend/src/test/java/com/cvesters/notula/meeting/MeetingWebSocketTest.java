package com.cvesters.notula.meeting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.cvesters.notula.block.BlockService;
import com.cvesters.notula.common.domain.Origin;
import com.cvesters.notula.common.domain.TextUpdate;
import com.cvesters.notula.common.exception.BusyEntityException;
import com.cvesters.notula.common.exception.MissingEntityException;
import com.cvesters.notula.session.TestSession;
import com.cvesters.notula.test.FrameHandler;
import com.cvesters.notula.test.WebSocketTest;
import com.cvesters.notula.textblock.TextBlockService;
import com.cvesters.notula.topic.TopicService;

class MeetingWebSocketTest extends WebSocketTest {

	private static final TestSession SESSION =
			TestSession.EDUARDO_CHRISTIANSEN_SPORER;

	private static final Origin ORIGIN = new Origin(SESSION.principal(),
			CLIENT_ID);

	private static final TestMeeting MEETING = TestMeeting.SPORER_PROJECT;

	private static final String ENDPOINT =
			"/app/meetings/" + MEETING.getId() + "/changes";

	private static final UUID CHANGE_ID = UUID
			.fromString("7c6f0d54-2f70-4a1e-9f5a-1d4c8b2e0a11");

	@MockitoBean
	private TopicService topicService;

	@MockitoBean
	private BlockService blockService;

	@MockitoBean
	private TextBlockService textBlockService;

	@MockitoBean
	private MeetingService meetingService;

	private static byte[] payload(final String change) {
		return change.getBytes(StandardCharsets.UTF_8);
	}

	@Nested
	class Submit {

		@Test
		void addTopic() throws Exception {
			connect(SESSION);
			send(ENDPOINT, CHANGE_ID, payload("""
					{
						"type": "ADD_TOPIC",
						"sequenceId": 2,
						"name": "Blockers"
					}
					"""));

			verify(topicService, timeout(WAIT_TIMEOUT.toMillis()))
					.create(eq(ORIGIN), eq(MEETING.getId()), argThat(action -> {
						assertThat(action.getSequenceId()).isEqualTo(2);
						assertThat(action.getName()).isEqualTo("Blockers");
						return true;
					}));
		}

		@Test
		void renameMeeting() throws Exception {
			connect(SESSION);
			send(ENDPOINT, CHANGE_ID, payload("""
					{
						"type": "RENAME_MEETING",
						"position": 0,
						"length": 3,
						"value": "Renamed"
					}
					"""));

			verify(meetingService, timeout(WAIT_TIMEOUT.toMillis()))
					.update(eq(ORIGIN), eq(MEETING.getId()), argThat(action -> {
						final var update = (TextUpdate<?>) action;

						assertThat(update.getPosition()).isEqualTo(0);
						assertThat(update.getLength()).isEqualTo(3);
						assertThat(update.getValue()).isEqualTo("Renamed");
						return true;
					}));
		}

		@Test
		void editTextBlock() throws Exception {
			connect(SESSION);
			send(ENDPOINT, CHANGE_ID, payload("""
					{
						"type": "EDIT_TEXT_BLOCK",
						"block": 61,
						"position": 4,
						"length": 2,
						"value": "new"
					}
					"""));

			verify(textBlockService, timeout(WAIT_TIMEOUT.toMillis()))
					.update(eq(ORIGIN), eq(MEETING.getId()), eq(61L),
							argThat(action -> {
								final var splice = (TextUpdate<?>) action;

								assertThat(splice.getPosition()).isEqualTo(4);
								assertThat(splice.getLength()).isEqualTo(2);
								assertThat(splice.getValue()).isEqualTo("new");
								return true;
							}));
		}

		@Test
		void addBlock() throws Exception {
			connect(SESSION);
			send(ENDPOINT, CHANGE_ID, payload("""
					{
						"type": "ADD_BLOCK",
						"topic": 32,
						"blockType": "TEXT",
						"sequenceId": 1
					}
					"""));

			verify(blockService, timeout(WAIT_TIMEOUT.toMillis()))
					.create(eq(ORIGIN), eq(MEETING.getId()), argThat(action -> {
						assertThat(action.getTopicId()).isEqualTo(32L);
						assertThat(action.getSequenceId()).isEqualTo(1);
						return true;
					}));
		}

		@Test
		void missing() throws Exception {
			doThrow(new MissingEntityException()).when(topicService)
					.delete(any(), anyLong(), anyLong());

			connect(SESSION);
			final FrameHandler rejections =
					subscribeToRejections();
			send(ENDPOINT, CHANGE_ID, payload("""
					{ "type": "REMOVE_TOPIC", "topic": 32 }
					"""));

			assertThat(rejections.getResponse())
					.succeedsWithin(WAIT_TIMEOUT.toSeconds(), TimeUnit.SECONDS)
					.isNotNull()
					.satisfies(rejected -> {
						assertThat(rejected).contains(CHANGE_ID.toString());
						assertThat(rejected).contains("\"retryable\":false");
						assertThat(rejected).doesNotContain("\"reason\":null");
					});
		}

		@Test
		void busy() throws Exception {
			doThrow(new BusyEntityException("Timed out")).when(topicService)
					.delete(any(), anyLong(), anyLong());

			connect(SESSION);
			final FrameHandler rejections =
					subscribeToRejections();
			send(ENDPOINT, CHANGE_ID, payload("""
					{ "type": "REMOVE_TOPIC", "topic": 32 }
					"""));

			assertThat(rejections.getResponse())
					.succeedsWithin(WAIT_TIMEOUT.toSeconds(), TimeUnit.SECONDS)
					.isNotNull()
					.satisfies(rejected -> assertThat(rejected)
							.contains("\"retryable\":true"));
		}

		@Test
		void success() throws Exception {
			when(topicService.create(any(), anyLong(), any()))
					.thenReturn(null);

			connect(SESSION);
			final FrameHandler rejections =
					subscribeToRejections();
			send(ENDPOINT, CHANGE_ID, payload("""
					{
						"type": "ADD_TOPIC",
						"sequenceId": 0,
						"name": "Blockers"
					}
					"""));

			verify(topicService, timeout(WAIT_TIMEOUT.toMillis()))
					.create(any(), anyLong(), any());

			assertThat(rejections.getResponse()).isNotDone();
		}

		@Test
		void invalid() throws Exception {
			connect(SESSION);
			final FrameHandler rejections = subscribeToRejections();
			send(ENDPOINT, CHANGE_ID, payload("""
					{ "type": "ADD_TOPIC", "sequenceId": -1, "name": "x" }
					"""));

			assertThat(rejections.getResponse())
					.succeedsWithin(WAIT_TIMEOUT.toSeconds(), TimeUnit.SECONDS)
					.isNotNull()
					.satisfies(rejected -> {
						assertThat(rejected).contains(CHANGE_ID.toString());
						assertThat(rejected).contains("\"retryable\":false");
					});
		}

		@Test
		void unreadable() throws Exception {
			connect(SESSION);
			final FrameHandler rejections = subscribeToRejections();
			send(ENDPOINT, CHANGE_ID, payload("""
					{ "type": "NOT_A_CHANGE" }
					"""));

			assertThat(rejections.getResponse())
					.succeedsWithin(WAIT_TIMEOUT.toSeconds(), TimeUnit.SECONDS)
					.isNotNull()
					.satisfies(rejected -> {
						assertThat(rejected).contains(CHANGE_ID.toString());
						assertThat(rejected).doesNotContain("\"reason\":null");
					});
		}

		@Test
		void unnamed() throws Exception {
			connect(SESSION);
			final FrameHandler rejections = subscribeToRejections();
			send(ENDPOINT, payload("""
					{ "type": "NOT_A_CHANGE" }
					"""));

			assertThat(rejections.getResponse())
					.succeedsWithin(WAIT_TIMEOUT.toSeconds(), TimeUnit.SECONDS)
					.isNotNull()
					.satisfies(rejected -> {
						assertThat(rejected).contains("\"id\":null");
						assertThat(rejected).doesNotContain("\"reason\":null");
					});
		}
	}
}
