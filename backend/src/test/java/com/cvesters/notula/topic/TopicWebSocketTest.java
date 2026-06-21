package com.cvesters.notula.topic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.messaging.simp.stomp.ConnectionLostException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.common.exception.MissingEntityException;
import com.cvesters.notula.meeting.TestMeeting;
import com.cvesters.notula.session.TestSession;
import com.cvesters.notula.test.FrameHandler;
import com.cvesters.notula.test.WebSocketTest;
import com.cvesters.notula.topic.bdo.TopicAction;

public class TopicWebSocketTest extends WebSocketTest {

	private static final String DESTINATION_PREFIX = "/app/meetings";
	private static final String DESTINATION_SUFFIX = "/topics";

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
	private static final Principal PRINCIPAL = SESSION.principal();
	private static final TestMeeting MEETING = TestMeeting.SPORER_PROJECT;

	@MockitoBean
	private TopicService topicService;

	@Nested
	class Create {

		@ParameterizedTest
		@ValueSource(strings = { "topic", "!@#$%^&*(){}[]|:;'<>,.?/",
				"Встреча: 你好 مرحبا" })
		void success(final String name) throws Exception {
			final byte[] payload = getRequestPayload(name);

			connect(SESSION);
			send(getDestination(MEETING.getId()), payload);

			final var action = new TopicAction.Create(name);
			final var matcher = new TopicActionMatcher.Create(action);
			verify(topicService, timeout(WAIT_TIMEOUT.toMillis())).create(
					eq(PRINCIPAL), eq(MEETING.getId()),
					argThat(matcher::matches));
		}

		@Test
		void notFound() throws Exception {
			final var topic = TestTopic.SPORER_PROJECT_BLOCKERS;
			final byte[] payload = getRequestPayload(topic.getName());

			when(topicService.create(any(), anyLong(), any()))
					.thenThrow(new MissingEntityException());

			connect(SESSION);
			final FrameHandler errorFrameHandler = subscribeToErrors();
			send(getDestination(MEETING.getId()), payload);

			assertThat(errorFrameHandler.getResponse())
					.succeedsWithin(WAIT_TIMEOUT.toSeconds(), TimeUnit.SECONDS)
					.isNotNull()
					.satisfies(
							message -> assertThat(message).startsWith("Error"));
		}

		@Test
		void invalid() throws Exception {
			final byte[] dto = "{\"name\": null}"
					.getBytes(StandardCharsets.UTF_8);

			connect(SESSION);
			final FrameHandler errorFrameHandler = subscribeToErrors();
			send(getDestination(MEETING.getId()), dto);

			verifyNoInteractions(topicService);
			assertThat(errorFrameHandler.getResponse())
					.succeedsWithin(WAIT_TIMEOUT.toSeconds(), TimeUnit.SECONDS)
					.isNotNull()
					.satisfies(
							message -> assertThat(message).startsWith("Error"));
		}

		@Test
		void unauthenticated() throws Exception {
			final byte[] payload = getRequestPayload("topic");

			connect();
			send(getDestination(MEETING.getId()), payload);

			assertThat(stompSessionHandler.getError())
					.succeedsWithin(WAIT_TIMEOUT.toSeconds(), TimeUnit.SECONDS)
					.isInstanceOf(ConnectionLostException.class);
		}

		private String getDestination(final long meetingId) {
			return DESTINATION_PREFIX + "/" + meetingId + DESTINATION_SUFFIX;
		}

		private byte[] getRequestPayload(final String name) {
			final String json = """
					{
						"name": "%s"
					}
					""".formatted(name);

			return json.getBytes(StandardCharsets.UTF_8);
		}
	}

	@Nested
	class UpdateName {

		private static final TestTopic TOPIC = TestTopic.SPORER_PROJECT_BLOCKERS;

		@ParameterizedTest
		@ValueSource(strings = { "topic", "!@#$%^&*(){}[]|:;'<>,.?/",
				"Встреча: 你好 مرحبا" })
		void success(final String name) throws Exception {
			final byte[] payload = getRequestPayload(name);

			connect(SESSION);
			send(getDestination(MEETING.getId(), TOPIC.getId()), payload);

			final var expected = new TopicAction.UpdateName(5, 2, name);
			final var matcher = new TopicActionMatcher.UpdateName(expected);
			verify(topicService, timeout(WAIT_TIMEOUT.toMillis())).update(
					eq(PRINCIPAL), eq(MEETING.getId()), eq(TOPIC.getId()),
					argThat(matcher::matches));
		}

		@Test
		void notFound() throws Exception {
			final byte[] payload = getRequestPayload(TOPIC.getName());

			when(topicService.update(any(), anyLong(), anyLong(), any()))
					.thenThrow(new MissingEntityException());

			connect(SESSION);
			final FrameHandler errorFrameHandler = subscribeToErrors();
			send(getDestination(MEETING.getId(), TOPIC.getId()), payload);

			assertThat(errorFrameHandler.getResponse())
					.succeedsWithin(WAIT_TIMEOUT.toSeconds(), TimeUnit.SECONDS)
					.isNotNull()
					.satisfies(
							message -> assertThat(message).startsWith("Error"));
		}

		@Test
		void unauthenticated() throws Exception {
			final byte[] payload = getRequestPayload("Updated");

			connect();
			send(getDestination(MEETING.getId(), TOPIC.getId()), payload);

			assertThat(stompSessionHandler.getError())
					.succeedsWithin(WAIT_TIMEOUT.toSeconds(), TimeUnit.SECONDS)
					.isInstanceOf(ConnectionLostException.class);
		}

		private byte[] getRequestPayload(final String name) {
			final String json = """
					{
						"action": "UPDATE_NAME",
						"position": 5,
						"length": 2,
						"value": "%s"
					}
					""".formatted(name);

			return json.getBytes(StandardCharsets.UTF_8);
		}

		private String getDestination(final long meetingId,
				final long topicId) {
			return DESTINATION_PREFIX + "/" + meetingId + DESTINATION_SUFFIX
					+ "/" + topicId;
		}
	}

	@Nested
	class Delete {

		private static final TestTopic TOPIC = TestTopic.SPORER_PROJECT_BLOCKERS;

		@Test
		void success() throws Exception {
			final byte[] payload = getRequestPayload();

			connect(SESSION);
			send(getDestination(MEETING.getId(), TOPIC.getId()), payload);

			verify(topicService, timeout(WAIT_TIMEOUT.toMillis()))
					.delete(PRINCIPAL, MEETING.getId(), TOPIC.getId());
		}

		@Test
		void notFound() throws Exception {
			final byte[] payload = getRequestPayload();

			doThrow(new MissingEntityException()).when(topicService)
					.delete(any(), anyLong(), anyLong());

			connect(SESSION);
			final FrameHandler errorFrameHandler = subscribeToErrors();
			send(getDestination(MEETING.getId(), TOPIC.getId()), payload);

			assertThat(errorFrameHandler.getResponse())
					.succeedsWithin(WAIT_TIMEOUT.toSeconds(), TimeUnit.SECONDS)
					.isNotNull()
					.satisfies(
							message -> assertThat(message).startsWith("Error"));
		}

		@Test
		void unauthenticated() throws Exception {
			final byte[] payload = getRequestPayload();

			connect();
			send(getDestination(MEETING.getId(), TOPIC.getId()), payload);

			assertThat(stompSessionHandler.getError())
					.succeedsWithin(WAIT_TIMEOUT.toSeconds(), TimeUnit.SECONDS)
					.isInstanceOf(ConnectionLostException.class);
		}

		private byte[] getRequestPayload() {
			return new byte[0];
		}

		private String getDestination(final long meetingId,
				final long topicId) {
			return DESTINATION_PREFIX + "/" + meetingId + DESTINATION_SUFFIX
					+ "/" + topicId + "/delete";
		}
	}
}
