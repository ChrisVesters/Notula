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
import java.util.stream.Stream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.messaging.simp.stomp.ConnectionLostException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.cvesters.notula.common.domain.Minutes;
import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.common.exception.MissingEntityException;
import com.cvesters.notula.meeting.TestMeeting;
import com.cvesters.notula.session.TestSession;
import com.cvesters.notula.test.FrameHandler;
import com.cvesters.notula.test.WebSocketTest;
import com.cvesters.notula.topic.bdo.TopicAction;

public class TopicWebSocketTest extends WebSocketTest {

	private static final String DESTINATION_PREFIX = "/app/topics";

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
	private static final Principal PRINCIPAL = SESSION.principal();
	private static final TestMeeting MEETING = TestMeeting.SPORER_PROJECT;

	@MockitoBean
	private TopicService topicService;

	@Nested
	class Create {

		private static final String ENDPOINT = DESTINATION_PREFIX + "/create";

		@ParameterizedTest
		@ValueSource(strings = { "topic", "!@#$%^&*(){}[]|:;'<>,.?/",
				"Встреча: 你好 مرحبا" })
		void success(final String name) throws Exception {
			final int sequenceId = 2;
			final byte[] payload = getRequestPayload(sequenceId, name);

			connect(SESSION);
			send(ENDPOINT, payload);

			final var action = new TopicAction.Create(MEETING.getId(),
					sequenceId, name);
			final var matcher = new TopicActionMatcher.Create(action);
			verify(topicService, timeout(WAIT_TIMEOUT.toMillis()))
					.create(eq(PRINCIPAL), argThat(matcher::matches));
		}

		@Test
		void notFound() throws Exception {
			final var topic = TestTopic.SPORER_PROJECT_BLOCKERS;
			final byte[] payload = getRequestPayload(topic.getSequenceId(),
					topic.getName());

			when(topicService.create(any(), any()))
					.thenThrow(new MissingEntityException());

			connect(SESSION);
			final FrameHandler errorFrameHandler = subscribeToErrors();
			send(ENDPOINT, payload);

			assertThat(errorFrameHandler.getResponse())
					.succeedsWithin(WAIT_TIMEOUT.toSeconds(), TimeUnit.SECONDS)
					.isNotNull()
					.satisfies(
							message -> assertThat(message).startsWith("Error"));
		}

		@ParameterizedTest(name = "[{index}] name = {0}")
		@MethodSource("invalidPayloadCases")
		void invalidPayload(final String name, final String body)
				throws Exception {
			final byte[] payload = body.getBytes(StandardCharsets.UTF_8);

			connect(SESSION);
			final FrameHandler errorFrameHandler = subscribeToErrors();
			send(ENDPOINT, payload);

			assertThat(errorFrameHandler.getResponse())
					.succeedsWithin(WAIT_TIMEOUT.toSeconds(), TimeUnit.SECONDS)
					.isNotNull()
					.satisfies(
							message -> assertThat(message).startsWith("Error"));

			verifyNoInteractions(topicService);
		}

		private static Stream<Arguments> invalidPayloadCases() {
			return Stream.of(Arguments.of("meetingId missing", """
					{
						"sequenceId": 2,
						"name": "topic"
					}
					"""), Arguments.of("meetingId null", """
					{
						"meetingId": null,
						"sequenceId": 2,
						"name": "topic"
					}
					"""), Arguments.of("sequenceId missing", """
					{
						"meetingId": 1,
						"name": "topic"
					}
					"""), Arguments.of("sequenceId null", """
					{
						"meetingId": 1,
						"sequenceId": null,
						"name": "topic"
					}
					"""), Arguments.of("sequenceId negative", """
					{
						"meetingId": 1,
						"sequenceId": -2,
						"name": "topic"
					}
					"""), Arguments.of("name missing", """
					{
						"meetingId": 1,
						"sequenceId": 2
					}
					"""), Arguments.of("name null", """
					{
						"meetingId": 1,
						"sequenceId": 2,
						"name": null
					}
					"""));
		}

		@Test
		void unauthenticated() throws Exception {
			final byte[] payload = getRequestPayload(0, "topic");

			connect();
			send(ENDPOINT, payload);

			assertThat(stompSessionHandler.getError())
					.succeedsWithin(WAIT_TIMEOUT.toSeconds(), TimeUnit.SECONDS)
					.isInstanceOf(ConnectionLostException.class);
		}

		private byte[] getRequestPayload(final int sequenceId,
				final String name) {
			final String json = """
					{
						"meetingId": %d,
						"sequenceId": %d,
						"name": "%s"
					}
					""".formatted(MEETING.getId(), sequenceId, name);

			return json.getBytes(StandardCharsets.UTF_8);
		}
	}

	@Nested
	class UpdateName {

		private static final String ENDPOINT = DESTINATION_PREFIX + "/update";

		private static final TestTopic TOPIC = TestTopic.SPORER_PROJECT_BLOCKERS;

		@ParameterizedTest
		@ValueSource(strings = { "topic", "!@#$%^&*(){}[]|:;'<>,.?/",
				"Встреча: 你好 مرحبا" })
		void success(final String name) throws Exception {
			final byte[] payload = getRequestPayload(name);

			connect(SESSION);
			send(ENDPOINT, payload);

			final var expected = new TopicAction.UpdateName(5, 2, name);
			final var matcher = new TopicActionMatcher.UpdateName(expected);
			verify(topicService, timeout(WAIT_TIMEOUT.toMillis())).update(
					eq(PRINCIPAL), eq(TOPIC.getId()),
					argThat(matcher::matches));
		}

		@Test
		void notFound() throws Exception {
			final byte[] payload = getRequestPayload(TOPIC.getName());

			when(topicService.update(any(), anyLong(), any()))
					.thenThrow(new MissingEntityException());

			connect(SESSION);
			final FrameHandler errorFrameHandler = subscribeToErrors();
			send(ENDPOINT, payload);

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
			send(ENDPOINT, payload);

			assertThat(stompSessionHandler.getError())
					.succeedsWithin(WAIT_TIMEOUT.toSeconds(), TimeUnit.SECONDS)
					.isInstanceOf(ConnectionLostException.class);
		}

		@ParameterizedTest(name = "[{index}] {0}")
		@MethodSource("invalidPayloadCases")
		void invalidPayload(final String name, final String body)
				throws Exception {
			final byte[] payload = body.getBytes(StandardCharsets.UTF_8);

			connect(SESSION);
			final FrameHandler errorFrameHandler = subscribeToErrors();
			send(ENDPOINT, payload);

			assertThat(errorFrameHandler.getResponse())
					.succeedsWithin(WAIT_TIMEOUT.toSeconds(), TimeUnit.SECONDS)
					.isNotNull()
					.satisfies(
							message -> assertThat(message).startsWith("Error"));

			verifyNoInteractions(topicService);
		}

		private static Stream<Arguments> invalidPayloadCases() {
			return Stream.of(Arguments.of("meetingId missing", """
					{
						"topicId": 2,
						"action": "UPDATE_NAME",
						"position": 5,
						"length": 2,
						"value": "topic"
					}
					"""), Arguments.of("topicId missing", """
					{
						"meetingId": 1,
						"action": "UPDATE_NAME",
						"position": 5,
						"length": 2,
						"value": "topic"
					}
					"""), Arguments.of("position missing", """
					{
						"meetingId": 1,
						"topicId": 2,
						"action": "UPDATE_NAME",
						"length": 2,
						"value": "topic"
					}
					"""), Arguments.of("length missing", """
					{
						"meetingId": 1,
						"topicId": 2,
						"action": "UPDATE_NAME",
						"position": 5,
						"value": "topic"
					}
					"""), Arguments.of("value missing", """
					{
						"meetingId": 1,
						"topicId": 2,
						"action": "UPDATE_NAME",
						"position": 5,
						"length": 2
					}
					"""), Arguments.of("position negative", """
					{
						"meetingId": 1,
						"topicId": 2,
						"action": "UPDATE_NAME",
						"position": -5,
						"length": 2,
						"value": "topic"
					}
					"""), Arguments.of("length negative", """
					{
						"meetingId": 1,
						"topicId": 2,
						"action": "UPDATE_NAME",
						"position": 5,
						"length": -2,
						"value": "topic"
					}
					"""), Arguments.of("value null", """
					{
						"meetingId": 1,
						"topicId": 2,
						"action": "UPDATE_NAME",
						"position": 5,
						"length": 2,
						"value": null
					}
					"""));
		}

		private byte[] getRequestPayload(final String name) {
			final String json = """
					{
						"meetingId": %d,
						"topicId": %d,
						"action": "UPDATE_NAME",
						"position": 5,
						"length": 2,
						"value": "%s"
					}
					""".formatted(MEETING.getId(), TOPIC.getId(), name);

			return json.getBytes(StandardCharsets.UTF_8);
		}
	}

	@Nested
	class UpdateDescription {

		private static final String ENDPOINT = DESTINATION_PREFIX + "/update";

		private static final TestTopic TOPIC = TestTopic.SPORER_PROJECT_BLOCKERS;

		@ParameterizedTest
		@ValueSource(strings = { "topic", "!@#$%^&*(){}[]|:;'<>,.?/",
				"Встреча: 你好 مرحبا" })
		void success(final String description) throws Exception {
			final byte[] payload = getRequestPayload(description);

			connect(SESSION);
			send(ENDPOINT, payload);

			final var expected = new TopicAction.UpdateDescription(5, 2,
					description);
			final var matcher = new TopicActionMatcher.UpdateDescription(
					expected);
			verify(topicService, timeout(WAIT_TIMEOUT.toMillis())).update(
					eq(PRINCIPAL), eq(TOPIC.getId()),
					argThat(matcher::matches));
		}

		@Test
		void notFound() throws Exception {
			final byte[] payload = getRequestPayload(TOPIC.getDescription());

			when(topicService.update(any(), anyLong(), any()))
					.thenThrow(new MissingEntityException());

			connect(SESSION);
			final FrameHandler errorFrameHandler = subscribeToErrors();
			send(ENDPOINT, payload);

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
			send(ENDPOINT, payload);

			assertThat(stompSessionHandler.getError())
					.succeedsWithin(WAIT_TIMEOUT.toSeconds(), TimeUnit.SECONDS)
					.isInstanceOf(ConnectionLostException.class);
		}

		@ParameterizedTest(name = "[{index}] {0}")
		@MethodSource("invalidPayloadCases")
		void invalidPayload(final String name, final String body)
				throws Exception {
			final byte[] payload = body.getBytes(StandardCharsets.UTF_8);

			connect(SESSION);
			final FrameHandler errorFrameHandler = subscribeToErrors();
			send(ENDPOINT, payload);

			assertThat(errorFrameHandler.getResponse())
					.succeedsWithin(WAIT_TIMEOUT.toSeconds(), TimeUnit.SECONDS)
					.isNotNull()
					.satisfies(
							message -> assertThat(message).startsWith("Error"));

			verifyNoInteractions(topicService);
		}

		private static Stream<Arguments> invalidPayloadCases() {
			return Stream.of(Arguments.of("meetingId missing", """
					{
						"topicId": 2,
						"action": "UPDATE_DESCRIPTION",
						"position": 5,
						"length": 2,
						"value": "topic"
					}
					"""), Arguments.of("topicId missing", """
					{
						"meetingId": 1,
						"action": "UPDATE_DESCRIPTION",
						"position": 5,
						"length": 2,
						"value": "topic"
					}
					"""), Arguments.of("position missing", """
					{
						"meetingId": 1,
						"topicId": 2,
						"action": "UPDATE_DESCRIPTION",
						"length": 2,
						"value": "topic"
					}
					"""), Arguments.of("length missing", """
					{
						"meetingId": 1,
						"topicId": 2,
						"action": "UPDATE_DESCRIPTION",
						"position": 5,
						"value": "topic"
					}
					"""), Arguments.of("value missing", """
					{
						"meetingId": 1,
						"topicId": 2,
						"action": "UPDATE_DESCRIPTION",
						"position": 5,
						"length": 2
					}
					"""), Arguments.of("position negative", """
					{
						"meetingId": 1,
						"topicId": 2,
						"action": "UPDATE_DESCRIPTION",
						"position": -5,
						"length": 2,
						"value": "topic"
					}
					"""), Arguments.of("length negative", """
					{
						"meetingId": 1,
						"topicId": 2,
						"action": "UPDATE_DESCRIPTION",
						"position": 5,
						"length": -2,
						"value": "topic"
					}
					"""), Arguments.of("value null", """
					{
						"meetingId": 1,
						"topicId": 2,
						"action": "UPDATE_DESCRIPTION",
						"position": 5,
						"length": 2,
						"value": null
					}
					"""));
		}

		private byte[] getRequestPayload(final String description) {
			final String json = """
					{
						"meetingId": %d,
						"topicId": %d,
						"action": "UPDATE_DESCRIPTION",
						"position": 5,
						"length": 2,
						"value": "%s"
					}
					""".formatted(MEETING.getId(), TOPIC.getId(), description);

			return json.getBytes(StandardCharsets.UTF_8);
		}
	}

	@Nested
	class UpdateDuration {

		private static final String ENDPOINT = DESTINATION_PREFIX + "/update";

		private static final TestTopic TOPIC = TestTopic.SPORER_PROJECT_BLOCKERS;

		@ParameterizedTest
		@ValueSource(ints = { 1, 45, Integer.MAX_VALUE })
		void success(final int duration) throws Exception {
			final byte[] payload = getRequestPayload(duration);

			connect(SESSION);
			send(ENDPOINT, payload);

			final var expected = new TopicAction.UpdateDuration(
					new Minutes(duration));
			final var matcher = new TopicActionMatcher.UpdateDuration(expected);
			verify(topicService, timeout(WAIT_TIMEOUT.toMillis())).update(
					eq(PRINCIPAL), eq(TOPIC.getId()),
					argThat(matcher::matches));
		}

		@Test
		void clearDuration() throws Exception {
			final byte[] payload = getRequestPayload(null);

			connect(SESSION);
			send(ENDPOINT, payload);

			final var expected = new TopicAction.UpdateDuration(null);
			final var matcher = new TopicActionMatcher.UpdateDuration(expected);
			verify(topicService, timeout(WAIT_TIMEOUT.toMillis())).update(
					eq(PRINCIPAL), eq(TOPIC.getId()),
					argThat(matcher::matches));
		}

		@Test
		void notFound() throws Exception {
			final byte[] payload = getRequestPayload(45);

			when(topicService.update(any(), anyLong(), any()))
					.thenThrow(new MissingEntityException());

			connect(SESSION);
			final FrameHandler errorFrameHandler = subscribeToErrors();
			send(ENDPOINT, payload);

			assertThat(errorFrameHandler.getResponse())
					.succeedsWithin(WAIT_TIMEOUT.toSeconds(), TimeUnit.SECONDS)
					.isNotNull()
					.satisfies(
							message -> assertThat(message).startsWith("Error"));
		}

		@Test
		void unauthenticated() throws Exception {
			final byte[] payload = getRequestPayload(45);

			connect();
			send(ENDPOINT, payload);

			assertThat(stompSessionHandler.getError())
					.succeedsWithin(WAIT_TIMEOUT.toSeconds(), TimeUnit.SECONDS)
					.isInstanceOf(ConnectionLostException.class);
		}

		@ParameterizedTest(name = "[{index}] {0}")
		@MethodSource("invalidPayloadCases")
		void invalidPayload(final String name, final String body)
				throws Exception {
			final byte[] payload = body.getBytes(StandardCharsets.UTF_8);

			connect(SESSION);
			final FrameHandler errorFrameHandler = subscribeToErrors();
			send(ENDPOINT, payload);

			assertThat(errorFrameHandler.getResponse())
					.succeedsWithin(WAIT_TIMEOUT.toSeconds(), TimeUnit.SECONDS)
					.isNotNull()
					.satisfies(
							message -> assertThat(message).startsWith("Error"));

			verifyNoInteractions(topicService);
		}

		private static Stream<Arguments> invalidPayloadCases() {
			return Stream.of(Arguments.of("meetingId missing", """
					{
						"topicId": 2,
						"action": "UPDATE_DURATION",
						"duration": 45
					}
					"""), Arguments.of("topicId missing", """
					{
						"meetingId": 1,
						"action": "UPDATE_DURATION",
						"duration": 45
					}
					"""), Arguments.of("duration zero", """
					{
						"meetingId": 1,
						"topicId": 2,
						"action": "UPDATE_DURATION",
						"duration": 0
					}
					"""), Arguments.of("duration negative", """
					{
						"meetingId": 1,
						"topicId": 2,
						"action": "UPDATE_DURATION",
						"duration": -45
					}
					"""), Arguments.of("duration non-integer", """
					{
						"meetingId": 1,
						"topicId": 2,
						"action": "UPDATE_DURATION",
						"duration": ten
					}
					"""));
		}

		private byte[] getRequestPayload(final Integer duration) {
			final String json = """
					{
						"meetingId": %d,
						"topicId": %d,
						"action": "UPDATE_DURATION",
						"duration": %s
					}
					""".formatted(MEETING.getId(), TOPIC.getId(), duration);

			return json.getBytes(StandardCharsets.UTF_8);
		}
	}

	@Nested
	class Delete {

		private static final String ENDPOINT = DESTINATION_PREFIX + "/delete";

		private static final TestTopic TOPIC = TestTopic.SPORER_PROJECT_BLOCKERS;

		@Test
		void success() throws Exception {
			final byte[] payload = getRequestPayload();

			connect(SESSION);
			send(ENDPOINT, payload);

			verify(topicService, timeout(WAIT_TIMEOUT.toMillis()))
					.delete(PRINCIPAL, TOPIC.getId());
		}

		@Test
		void notFound() throws Exception {
			final byte[] payload = getRequestPayload();

			doThrow(new MissingEntityException()).when(topicService)
					.delete(any(), anyLong());

			connect(SESSION);
			final FrameHandler errorFrameHandler = subscribeToErrors();
			send(ENDPOINT, payload);

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
			send(ENDPOINT, payload);

			assertThat(stompSessionHandler.getError())
					.succeedsWithin(WAIT_TIMEOUT.toSeconds(), TimeUnit.SECONDS)
					.isInstanceOf(ConnectionLostException.class);
		}

		private byte[] getRequestPayload() {
			final String json = """
					{
						"meetingId": %d,
						"topicId": %d
					}
					""".formatted(MEETING.getId(), TOPIC.getId());

			return json.getBytes(StandardCharsets.UTF_8);
		}
	}
}
