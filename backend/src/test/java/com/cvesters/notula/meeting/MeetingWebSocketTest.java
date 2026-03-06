package com.cvesters.notula.meeting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.lang.reflect.Type;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.meeting.bdo.MeetingDetails;
import com.cvesters.notula.meeting.dto.MeetingDetailsDto;
import com.cvesters.notula.session.TestSession;
import com.cvesters.notula.test.WithSession;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@WithSession(TestSession.EDUARDO_CHRISTIANSEN_SPORER)
class MeetingWebSocketTest {

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
	private static final Principal PRINCIPAL = SESSION.principal();
	private static final TestMeeting MEETING = TestMeeting.SPORER_PROJECT;

	@MockitoBean
	private MeetingService meetingService;

	@LocalServerPort
	private int port;

	private WebSocketStompClient stompClient;
	private StompSession stompSession;

	@BeforeEach
	void setup() throws Exception {
		WebSocketClient webSocketClient = new StandardWebSocketClient();
		stompClient = new WebSocketStompClient(webSocketClient);

		CompletableFuture<StompSession> future = stompClient.connectAsync(
				"ws://localhost:" + port + "/ws",
				new WebSocketHttpHeaders(),
				new StompSessionHandlerAdapter() {
				});

		stompSession = future.get(10, TimeUnit.SECONDS);
	}

	@AfterEach
	void teardown() throws Exception {
		if (stompSession != null && stompSession.isConnected()) {
			stompSession.disconnect();
		}
	}

	@Nested
	class Subscribe {

		@Test
		void success() throws Exception {
			final MeetingDetails details = new MeetingDetails(MEETING.info());
			when(meetingService.getDetails(PRINCIPAL, MEETING.getId()))
					.thenReturn(details);

			CompletableFuture<MeetingDetailsDto> result = new CompletableFuture<>();

			stompSession.subscribe("/app/meetings/" + MEETING.getId(),
					new StompFrameHandler() {
						@Override
						public Type getPayloadType(StompHeaders headers) {
							return MeetingDetailsDto.class;
						}

						@Override
						public void handleFrame(StompHeaders headers,
								Object payload) {
							result.complete((MeetingDetailsDto) payload);
						}
					});

			MeetingDetailsDto response = result.get(5, TimeUnit.SECONDS);
			assertThat(response).isNotNull();
			assertThat(response.info().id()).isEqualTo(MEETING.getId());
			assertThat(response.info().name()).isEqualTo(MEETING.getName());
		}

		@Test
		void multipleSubscriptions() throws Exception {
			final TestMeeting meeting1 = TestMeeting.SPORER_PROJECT;
			final TestMeeting meeting2 = TestMeeting.SPORER_RETRO;

			final MeetingDetails details1 = new MeetingDetails(
					meeting1.info());
			final MeetingDetails details2 = new MeetingDetails(
					meeting2.info());

			when(meetingService.getDetails(PRINCIPAL, meeting1.getId()))
					.thenReturn(details1);
			when(meetingService.getDetails(PRINCIPAL, meeting2.getId()))
					.thenReturn(details2);

			CompletableFuture<MeetingDetailsDto> result1 = new CompletableFuture<>();
			CompletableFuture<MeetingDetailsDto> result2 = new CompletableFuture<>();

			stompSession.subscribe("/app/meetings/" + meeting1.getId(),
					new StompFrameHandler() {
						@Override
						public Type getPayloadType(StompHeaders headers) {
							return MeetingDetailsDto.class;
						}

						@Override
						public void handleFrame(StompHeaders headers,
								Object payload) {
							result1.complete((MeetingDetailsDto) payload);
						}
					});

			stompSession.subscribe("/app/meetings/" + meeting2.getId(),
					new StompFrameHandler() {
						@Override
						public Type getPayloadType(StompHeaders headers) {
							return MeetingDetailsDto.class;
						}

						@Override
						public void handleFrame(StompHeaders headers,
								Object payload) {
							result2.complete((MeetingDetailsDto) payload);
						}
					});

			MeetingDetailsDto response1 = result1.get(5, TimeUnit.SECONDS);
			MeetingDetailsDto response2 = result2.get(5, TimeUnit.SECONDS);

			assertThat(response1.info().id()).isEqualTo(meeting1.getId());
			assertThat(response2.info().id()).isEqualTo(meeting2.getId());
			assertThat(response1.info().name())
					.isNotEqualTo(response2.info().name());
		}

		@Test
		void subscribeWithoutAccess() throws Exception {
			final TestMeeting meetingFromOtherOrg = TestMeeting.GLOVER_KICKOFF_2026;
			
			// The authorization manager should deny access before the handler
			// is invoked, so the subscription should fail or not receive data
			CompletableFuture<MeetingDetailsDto> result = new CompletableFuture<>();

			stompSession.subscribe("/app/meetings/" + meetingFromOtherOrg.getId(),
					new StompFrameHandler() {
						@Override
						public Type getPayloadType(StompHeaders headers) {
							return MeetingDetailsDto.class;
						}

						@Override
						public void handleFrame(StompHeaders headers,
								Object payload) {
							result.complete((MeetingDetailsDto) payload);
						}
					});

			// Wait briefly and verify no subscription response was received
			// (authorization should have been denied)
			try {
				result.get(2, TimeUnit.SECONDS);
				// If we get here, the subscription succeeded when it shouldn't have
				assertThat(false).as("Subscription should have been denied by authorization manager").isTrue();
			} catch (java.util.concurrent.TimeoutException e) {
				// Expected: subscription was rejected/not authorized
				assertThat(true).isTrue();
			}
		}
	}

	@Nested
	class Send {

		@Test
		void success() throws Exception {
			CompletableFuture<String> result = new CompletableFuture<>();

			stompSession.subscribe("/topic/meetings/" + MEETING.getId(),
					new StompFrameHandler() {
						@Override
						public Type getPayloadType(StompHeaders headers) {
							return String.class;
						}

						@Override
						public void handleFrame(StompHeaders headers,
								Object payload) {
							result.complete((String) payload);
						}
					});

			final String message = "This is a test message";
			stompSession.send("/app/meetings/" + MEETING.getId(), message);

			String response = result.get(5, TimeUnit.SECONDS);
			assertThat(response).isEqualTo("Echo: " + message);
		}

		@Test
		void emptyMessage() throws Exception {
			CompletableFuture<String> result = new CompletableFuture<>();

			stompSession.subscribe("/topic/meetings/" + MEETING.getId(),
					new StompFrameHandler() {
						@Override
						public Type getPayloadType(StompHeaders headers) {
							return String.class;
						}

						@Override
						public void handleFrame(StompHeaders headers,
								Object payload) {
							result.complete((String) payload);
						}
					});

			stompSession.send("/app/meetings/" + MEETING.getId(), "");

			String response = result.get(5, TimeUnit.SECONDS);
			assertThat(response).isEqualTo("Echo: ");
		}

		@Test
		void largeMessage() throws Exception {
			CompletableFuture<String> result = new CompletableFuture<>();

			stompSession.subscribe("/topic/meetings/" + MEETING.getId(),
					new StompFrameHandler() {
						@Override
						public Type getPayloadType(StompHeaders headers) {
							return String.class;
						}

						@Override
						public void handleFrame(StompHeaders headers,
								Object payload) {
							result.complete((String) payload);
						}
					});

			final String message = "A".repeat(10000);
			stompSession.send("/app/meetings/" + MEETING.getId(), message);

			String response = result.get(5, TimeUnit.SECONDS);
			assertThat(response).isEqualTo("Echo: " + message);
		}

		@Test
		void specialCharacters() throws Exception {
			CompletableFuture<String> result = new CompletableFuture<>();

			stompSession.subscribe("/topic/meetings/" + MEETING.getId(),
					new StompFrameHandler() {
						@Override
						public Type getPayloadType(StompHeaders headers) {
							return String.class;
						}

						@Override
						public void handleFrame(StompHeaders headers,
								Object payload) {
							result.complete((String) payload);
						}
					});

			final String message = "Test message with special chars: !@#$%^&*(){}[]|\\:;\"'<>,.?/";
			stompSession.send("/app/meetings/" + MEETING.getId(), message);

			String response = result.get(5, TimeUnit.SECONDS);
			assertThat(response).isEqualTo("Echo: " + message);
		}

		@Test
		void jsonPayload() throws Exception {
			CompletableFuture<String> result = new CompletableFuture<>();

			stompSession.subscribe("/topic/meetings/" + MEETING.getId(),
					new StompFrameHandler() {
						@Override
						public Type getPayloadType(StompHeaders headers) {
							return String.class;
						}

						@Override
						public void handleFrame(StompHeaders headers,
								Object payload) {
							result.complete((String) payload);
						}
					});

			final String message = "{\"key\":\"value\",\"number\":42}";
			stompSession.send("/app/meetings/" + MEETING.getId(), message);

			String response = result.get(5, TimeUnit.SECONDS);
			assertThat(response).isEqualTo("Echo: " + message);
		}

		@Test
		void unicodeMessage() throws Exception {
			CompletableFuture<String> result = new CompletableFuture<>();

			stompSession.subscribe("/topic/meetings/" + MEETING.getId(),
					new StompFrameHandler() {
						@Override
						public Type getPayloadType(StompHeaders headers) {
							return String.class;
						}

						@Override
						public void handleFrame(StompHeaders headers,
								Object payload) {
							result.complete((String) payload);
						}
					});

			final String message = "Встреча: 你好 مرحبا";
			stompSession.send("/app/meetings/" + MEETING.getId(), message);

			String response = result.get(5, TimeUnit.SECONDS);
			assertThat(response).isEqualTo("Echo: " + message);
		}
	}

	@Nested
	class Integration {

		@Test
		void subscribeAndSend() throws Exception {
			final MeetingDetails details = new MeetingDetails(MEETING.info());
			when(meetingService.getDetails(PRINCIPAL, MEETING.getId()))
					.thenReturn(details);

			CompletableFuture<MeetingDetailsDto> subscriptionResult = new CompletableFuture<>();
			CompletableFuture<String> messageResult = new CompletableFuture<>();

			// Subscribe to initial state
			stompSession.subscribe("/app/meetings/" + MEETING.getId(),
					new StompFrameHandler() {
						@Override
						public Type getPayloadType(StompHeaders headers) {
							return MeetingDetailsDto.class;
						}

						@Override
						public void handleFrame(StompHeaders headers,
								Object payload) {
							subscriptionResult.complete((MeetingDetailsDto) payload);
						}
					});

			// Subscribe to topic for message updates
			stompSession.subscribe("/topic/meetings/" + MEETING.getId(),
					new StompFrameHandler() {
						@Override
						public Type getPayloadType(StompHeaders headers) {
							return String.class;
						}

						@Override
						public void handleFrame(StompHeaders headers,
								Object payload) {
							messageResult.complete((String) payload);
						}
					});

			MeetingDetailsDto subscriptionResponse = subscriptionResult
					.get(5, TimeUnit.SECONDS);
			assertThat(subscriptionResponse).isNotNull();
			assertThat(subscriptionResponse.info().name())
					.isEqualTo(MEETING.getName());

			// Send a message through the handler
			final String message = "Integration test message";
			stompSession.send("/app/meetings/" + MEETING.getId(), message);

			String echoResponse = messageResult.get(5, TimeUnit.SECONDS);
			assertThat(echoResponse).isEqualTo("Echo: " + message);
		}
	}

}
