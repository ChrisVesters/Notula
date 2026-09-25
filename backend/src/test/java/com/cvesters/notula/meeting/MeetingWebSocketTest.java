package com.cvesters.notula.meeting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.cvesters.notula.common.domain.ChangeId;
import com.cvesters.notula.common.domain.Origin;
import com.cvesters.notula.common.exception.BusyEntityException;
import com.cvesters.notula.common.exception.MissingEntityException;
import com.cvesters.notula.meeting.bdo.MeetingScope;
import com.cvesters.notula.meeting.dto.TopicChangeDto;
import com.cvesters.notula.session.TestSession;
import com.cvesters.notula.test.FrameHandler;
import com.cvesters.notula.test.WebSocketTest;

class MeetingWebSocketTest extends WebSocketTest {

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;

	private static final Origin ORIGIN = new Origin(SESSION.principal(),
			CLIENT_ID);

	private static final TestMeeting MEETING = TestMeeting.SPORER_PROJECT;

	private static final String ENDPOINT = "/app/meetings/" + MEETING.getId()
			+ "/changes";

	private static final UUID CHANGE_ID = UUID
			.fromString("7c6f0d54-2f70-4a1e-9f5a-1d4c8b2e0a11");

	private static final long REVISION = 12L;

	@MockitoBean
	private ChangeService changeService;

	private static byte[] payload(final String change) {
		return change.getBytes(StandardCharsets.UTF_8);
	}

	private void commits() {
		when(changeService.apply(any(), any(), anyLong(), any()))
				.thenReturn(new MeetingScope(MEETING.getId(), REVISION));
	}

	@Nested
	class Submit {

		@Test
		void change() throws Exception {
			commits();

			connect(SESSION);
			send(ENDPOINT, CHANGE_ID, payload("""
					{
						"type": "ADD_TOPIC",
						"afterId": 2,
						"name": "Blockers"
					}
					"""));

			verify(changeService, timeout(WAIT_TIMEOUT.toMillis())).apply(
					eq(ORIGIN), eq(new ChangeId(CHANGE_ID)),
					eq(MEETING.getId()), argThat(change -> {
						final var add = (TopicChangeDto.Add) change;

						assertThat(add.afterId()).isEqualTo(2L);
						assertThat(add.name()).isEqualTo("Blockers");
						return true;
					}));
		}

		@Test
		void acknowledged() throws Exception {
			commits();

			connect(SESSION);
			final FrameHandler acknowledgements = subscribeToAcknowledgements();
			final FrameHandler rejections = subscribeToRejections();
			send(ENDPOINT, CHANGE_ID, payload("""
					{ "type": "REMOVE_TOPIC", "topic": 32 }
					"""));

			assertThat(acknowledgements.getResponse())
					.succeedsWithin(WAIT_TIMEOUT.toSeconds(), TimeUnit.SECONDS)
					.satisfies(acknowledged -> assertThat(acknowledged)
							.isEqualToIgnoringWhitespace("""
									{
										"id": "%s",
										"revision": %d
									}
									""".formatted(CHANGE_ID, REVISION)));
			assertThat(rejections.getResponse()).isNotDone();
		}

		@Test
		void missing() throws Exception {
			doThrow(new MissingEntityException()).when(changeService)
					.apply(any(), any(), anyLong(), any());

			connect(SESSION);
			final FrameHandler rejections = subscribeToRejections();
			final FrameHandler acknowledgements = subscribeToAcknowledgements();
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
			assertThat(acknowledgements.getResponse()).isNotDone();
		}

		@Test
		void busy() throws Exception {
			doThrow(new BusyEntityException("Timed out")).when(changeService)
					.apply(any(), any(), anyLong(), any());

			connect(SESSION);
			final FrameHandler rejections = subscribeToRejections();
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
		void invalid() throws Exception {
			connect(SESSION);
			final FrameHandler rejections = subscribeToRejections();
			send(ENDPOINT, CHANGE_ID, payload("""
					{ "type": "ADD_TOPIC", "afterId": -1, "name": "x" }
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
		void negativeBase() throws Exception {
			connect(SESSION);
			final FrameHandler rejections = subscribeToRejections();
			send(ENDPOINT, CHANGE_ID, payload("""
					{
						"type": "RENAME_TOPIC",
						"topic": 32,
						"base": -1,
						"position": 0,
						"length": 0,
						"value": "x"
					}
					"""));

			assertThat(rejections.getResponse())
					.succeedsWithin(WAIT_TIMEOUT.toSeconds(), TimeUnit.SECONDS)
					.isNotNull()
					.satisfies(rejected -> {
						assertThat(rejected).contains(CHANGE_ID.toString());
						assertThat(rejected).contains("\"retryable\":false");
					});
			verify(changeService, never()).apply(any(), any(), anyLong(), any());
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
