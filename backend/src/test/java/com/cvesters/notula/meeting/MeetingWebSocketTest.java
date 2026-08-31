package com.cvesters.notula.meeting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
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

import com.cvesters.notula.common.domain.Origin;
import com.cvesters.notula.common.exception.MissingEntityException;
import com.cvesters.notula.meeting.bdo.MeetingAction;
import com.cvesters.notula.session.TestSession;
import com.cvesters.notula.test.FrameHandler;
import com.cvesters.notula.test.WebSocketTest;

class MeetingWebSocketTest extends WebSocketTest {

	private static final String DESTINATION_PREFIX = "/app/meetings";

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
	private static final Origin ORIGIN = new Origin(SESSION.principal(),
			CLIENT_ID);
	private static final TestMeeting MEETING = TestMeeting.SPORER_PROJECT;

	@MockitoBean
	private MeetingService meetingService;

	@Nested
	class UpdateName {

		private static final String ENDPOINT = DESTINATION_PREFIX + "/update";

		@ParameterizedTest
		@ValueSource(strings = { "meet", "!@#$%^&*(){}[]|;'<>,.?/",
				"Встреча: 你好 مرحبا" })
		void success(final String name) throws Exception {
			final byte[] payload = getRequestPayload(MEETING.getId(), name);

			connect(SESSION);
			send(ENDPOINT, payload);

			final var expected = new MeetingAction.UpdateName(5, 2, name);
			final var matcher = new MeetingActionMatcher.UpdateName(expected);
			verify(meetingService, timeout(WAIT_TIMEOUT.toMillis())).update(
					eq(ORIGIN), eq(MEETING.getId()),
					argThat(matcher::matches));
		}

		@Test
		void notFound() throws Exception {
			final var meeting = TestMeeting.SPORER_PROJECT;
			final byte[] payload = getRequestPayload(MEETING.getId(),
					meeting.getName());

			when(meetingService.update(any(), anyLong(), any()))
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
			final byte[] payload = getRequestPayload(MEETING.getId(),
					"meeting");

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

			verifyNoInteractions(meetingService);
		}

		private static Stream<Arguments> invalidPayloadCases() {
			return Stream.of(Arguments.of("meetingId missing", """
					{
						"action": "UPDATE_NAME",
						"position": 5,
						"length": 2,
						"value": "meeting"
					}
					"""), Arguments.of("position missing", """
					{
						"meetingId": 1,
						"action": "UPDATE_NAME",
						"length": 2,
						"value": "meeting"
					}
					"""), Arguments.of("length missing", """
					{
						"meetingId": 1,
						"action": "UPDATE_NAME",
						"position": 5,
						"value": "meeting"
					}
					"""), Arguments.of("value missing", """
					{
						"meetingId": 1,
						"action": "UPDATE_NAME",
						"position": 5,
						"length": 2
					}
					"""), Arguments.of("meetingId null", """
					{
						"meetingId": null,
						"action": "UPDATE_NAME",
						"position": 5,
						"length": 2,
						"value": "meeting"
					}
					"""), Arguments.of("position negative", """
					{
						"meetingId": 1,
						"action": "UPDATE_NAME",
						"position": -1,
						"length": 2,
						"value": "meeting"
					}
					"""), Arguments.of("length negative", """
					{
						"meetingId": 1,
						"action": "UPDATE_NAME",
						"position": 5,
						"length": -2,
						"value": "meeting"
					}
					"""), Arguments.of("value null", """
					{
						"meetingId": 1,
						"action": "UPDATE_NAME",
						"position": 5,
						"length": 2,
						"value": null
					}
					"""));
		}

		private byte[] getRequestPayload(final long meetingId,
				final String name) {
			final String json = """
					{
						"meetingId": %d,
						"action": "UPDATE_NAME",
						"position": 5,
						"length": 2,
						"value": "%s"
					}
					""".formatted(meetingId, name);

			return json.getBytes(StandardCharsets.UTF_8);
		}
	}

	@Nested
	class UpdateDescription {

		private static final String ENDPOINT = DESTINATION_PREFIX + "/update";

		@ParameterizedTest
		@ValueSource(strings = { "meet", "!@#$%^&*(){}[]|;'<>,.?/",
				"Встреча: 你好 مرحبا" })
		void success(final String name) throws Exception {
			final byte[] payload = getRequestPayload(MEETING.getId(), name);

			connect(SESSION);
			send(ENDPOINT, payload);

			final var expected = new MeetingAction.UpdateDescription(5, 2,
					name);
			final var matcher = new MeetingActionMatcher.UpdateDescription(
					expected);
			verify(meetingService, timeout(WAIT_TIMEOUT.toMillis())).update(
					eq(ORIGIN), eq(MEETING.getId()),
					argThat(matcher::matches));
		}

		@Test
		void notFound() throws Exception {
			final var meeting = TestMeeting.SPORER_PROJECT;
			final byte[] payload = getRequestPayload(MEETING.getId(),
					meeting.getName());

			when(meetingService.update(any(), anyLong(), any()))
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
			final byte[] payload = getRequestPayload(MEETING.getId(),
					"meeting");

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

			verifyNoInteractions(meetingService);
		}

		private static Stream<Arguments> invalidPayloadCases() {
			return Stream.of(Arguments.of("meetingId missing", """
					{
						"action": "UPDATE_DESCRIPTION",
						"position": 5,
						"length": 2,
						"value": "meeting"
					}
					"""), Arguments.of("position missing", """
					{
						"meetingId": 1,
						"action": "UPDATE_DESCRIPTION",
						"length": 2,
						"value": "meeting"
					}
					"""), Arguments.of("length missing", """
					{
						"meetingId": 1,
						"action": "UPDATE_DESCRIPTION",
						"position": 5,
						"value": "meeting"
					}
					"""), Arguments.of("value missing", """
					{
						"meetingId": 1,
						"action": "UPDATE_DESCRIPTION",
						"position": 5,
						"length": 2
					}
					"""), Arguments.of("meetingId null", """
					{
						"meetingId": null,
						"action": "UPDATE_DESCRIPTION",
						"position": 5,
						"length": 2,
						"value": "meeting"
					}
					"""), Arguments.of("position negative", """
					{
						"meetingId": 1,
						"action": "UPDATE_DESCRIPTION",
						"position": -1,
						"length": 2,
						"value": "meeting"
					}
					"""), Arguments.of("length negative", """
					{
						"meetingId": 1,
						"action": "UPDATE_DESCRIPTION",
						"position": 5,
						"length": -2,
						"value": "meeting"
					}
					"""), Arguments.of("value null", """
					{
						"meetingId": 1,
						"action": "UPDATE_DESCRIPTION",
						"position": 5,
						"length": 2,
						"value": null
					}
					"""));
		}

		private byte[] getRequestPayload(final long meetingId,
				final String name) {
			final String json = """
					{
						"meetingId": %d,
						"action": "UPDATE_DESCRIPTION",
						"position": 5,
						"length": 2,
						"value": "%s"
					}
					""".formatted(meetingId, name);

			return json.getBytes(StandardCharsets.UTF_8);
		}
	}

}
