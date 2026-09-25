package com.cvesters.notula.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.stomp.ConnectionLostException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.common.exception.MissingEntityException;
import com.cvesters.notula.event.bdo.EventInfo;
import com.cvesters.notula.event.bdo.TopicMutation;
import com.cvesters.notula.meeting.TestMeeting;
import com.cvesters.notula.session.TestSession;
import com.cvesters.notula.test.FrameHandler;
import com.cvesters.notula.test.WebSocketTest;

class EventWebSocketTest extends WebSocketTest {

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
	private static final Principal PRINCIPAL = SESSION.principal();
	private static final TestMeeting MEETING = TestMeeting.SPORER_PROJECT;
	private static final long AFTER = 12L;
	private static final UUID OTHER_CLIENT_ID = UUID
			.fromString("5b2e7c91-0d4a-4f63-8e15-3a9c6d0b7f42");

	private static final String DESTINATION = "/app/meetings/"
			+ MEETING.getId() + "/events/" + AFTER;

	@MockitoBean
	private EventService eventService;

	@Nested
	class Replay {

		@Test
		void success() throws Exception {
			when(eventService.findAllSince(PRINCIPAL, MEETING.getId(), AFTER))
					.thenReturn(List.of(
							new EventInfo(7L, MEETING.getId(), AFTER + 1,
									PRINCIPAL.userId(), OTHER_CLIENT_ID, null,
									new TopicMutation.Remove(31L)),
							new EventInfo(8L, MEETING.getId(), AFTER + 2,
									PRINCIPAL.userId(), null, null,
									new TopicMutation.Remove(32L))));

			connect(SESSION);
			final FrameHandler frameHandler = subscribe(DESTINATION);

			final String response = assertThat(frameHandler.getResponse())
					.succeedsWithin(WAIT_TIMEOUT.toSeconds(), TimeUnit.SECONDS)
					.isNotNull()
					.actual();

			assertThat(response).isEqualToIgnoringWhitespace("""
					[
						{
							"revision": 13,
							"origin": {
								"userId": %d,
								"clientId": "%s"
							},
							"mutation": {
								"type": "REMOVE_TOPIC",
								"topic": 31
							}
						},
						{
							"revision": 14,
							"origin": {
								"userId": %d,
								"clientId": null
							},
							"mutation": {
								"type": "REMOVE_TOPIC",
								"topic": 32
							}
						}
					]
					""".formatted(PRINCIPAL.userId(), OTHER_CLIENT_ID,
					PRINCIPAL.userId()));
		}

		@Test
		void none() throws Exception {
			when(eventService.findAllSince(PRINCIPAL, MEETING.getId(), AFTER))
					.thenReturn(List.of());

			connect(SESSION);
			final FrameHandler frameHandler = subscribe(DESTINATION);

			assertThat(frameHandler.getResponse())
					.succeedsWithin(WAIT_TIMEOUT.toSeconds(), TimeUnit.SECONDS)
					.isEqualTo("[]");
		}

		@Test
		void notFound() throws Exception {
			when(eventService.findAllSince(any(), anyLong(), anyLong()))
					.thenThrow(MissingEntityException.class);

			connect(SESSION);
			final FrameHandler rejections = subscribeToRejections();
			subscribe(DESTINATION);

			assertThat(rejections.getResponse())
					.succeedsWithin(WAIT_TIMEOUT.toSeconds(), TimeUnit.SECONDS)
					.isNotNull()
					.satisfies(rejected -> {
						assertThat(rejected).contains("\"id\":null");
						assertThat(rejected).contains("\"retryable\":false");
					});
		}

		@Test
		void unauthenticated() throws Exception {
			connect();
			subscribe(DESTINATION);

			assertThat(stompSessionHandler.getError())
					.succeedsWithin(WAIT_TIMEOUT.toSeconds(), TimeUnit.SECONDS)
					.isInstanceOf(ConnectionLostException.class);
		}
	}
}
