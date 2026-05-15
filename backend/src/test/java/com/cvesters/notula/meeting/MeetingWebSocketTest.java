package com.cvesters.notula.meeting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
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
import com.cvesters.notula.meeting.bdo.MeetingAction;
import com.cvesters.notula.session.TestSession;
import com.cvesters.notula.test.FrameHandler;
import com.cvesters.notula.test.WebSocketTest;

class MeetingWebSocketTest extends WebSocketTest {

	private static final String DESTINATION_PREFIX = "/app/meetings/";

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
	private static final Principal PRINCIPAL = SESSION.principal();
	private static final TestMeeting MEETING = TestMeeting.SPORER_PROJECT;

	@MockitoBean
	private MeetingService meetingService;

	@Nested
	class UpdateName {

		@ParameterizedTest
		@ValueSource(strings = { "meet", "!@#$%^&*(){}[]|;'<>,.?/",
				"Встреча: 你好 مرحبا" })
		void success(final String name) throws Exception {
			final byte[] payload = getRequestPayload(name);

			connect(SESSION);
			send(getDestination(MEETING.getId()), payload);

			final var expected = new MeetingAction.UpdateName(5, 2, name);
			final var matcher = new MeetingActionMatcher.UpdateName(expected);
			verify(meetingService, timeout(WAIT_TIMEOUT.toMillis())).update(
					eq(PRINCIPAL), eq(MEETING.getId()),
					argThat(matcher::matches));
		}

		@Test
		void notFound() throws Exception {
			final var meeting = TestMeeting.SPORER_PROJECT;
			final byte[] payload = getRequestPayload(meeting.getName());

			when(meetingService.update(any(), anyLong(), any()))
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
		void unauthenticated() throws Exception {
			final byte[] payload = getRequestPayload("meeting");

			connect();
			send(getDestination(MEETING.getId()), payload);

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

		private String getDestination(final long meetingId) {
			return DESTINATION_PREFIX + meetingId;
		}
	}
}
