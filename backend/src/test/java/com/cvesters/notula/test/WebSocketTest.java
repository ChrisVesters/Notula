package com.cvesters.notula.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.cvesters.notula.TestContainerConfig;
import com.cvesters.notula.config.JwtAuthConverter;
import com.cvesters.notula.session.TestSession;

@Testcontainers
@Import(TestContainerConfig.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class WebSocketTest {

	protected static final Duration WAIT_TIMEOUT = Duration.ofSeconds(1);

	private static final Duration ACCESS_EXPIRATION = Duration.ofMinutes(30);

	protected static final UUID CLIENT_ID = UUID
			.fromString("9d4e1b06-7c52-4f38-b1a9-6e83d0c5f2b7");

	@MockitoBean
	private JwtDecoder jwtDecoder;

	@MockitoBean
	private JwtAuthConverter authManager;

	@LocalServerPort
	private int port;
	private String url;

	private WebSocketStompClient stompClient;
	private final List<StompSession> sessions = new ArrayList<>();
	private StompSession stompSession;

	protected final SessionHandler stompSessionHandler = new SessionHandler();

	@BeforeEach
	void setup() {
		final var webSocketClient = new StandardWebSocketClient();
		stompClient = new WebSocketStompClient(webSocketClient);
		url = "ws://localhost:" + port + "/ws";
	}

	@AfterEach
	void teardown() {
		sessions.stream()
				.filter(StompSession::isConnected)
				.forEach(StompSession::disconnect);
		sessions.clear();
		stompSession = null;
	}

	protected StompSession connect(final TestSession session) throws Exception {
		return connect(session, Instant.now().plus(ACCESS_EXPIRATION));
	}

	protected StompSession connect(final TestSession session,
			final Instant expiresAt) throws Exception {
		final String token = "token";
		final Jwt jwt = mock();
		when(jwtDecoder.decode(token)).thenReturn(jwt);
		when(jwt.getExpiresAt()).thenReturn(expiresAt);

		final var authToken = session.getAuthToken();
		when(authManager.convert(jwt)).thenReturn(authToken);

		final var stompHeaders = new StompHeaders();
		stompHeaders.add("Authorization", "Bearer " + token);

		return open(stompHeaders);
	}

	protected StompSession connect() throws Exception {
		return open(new StompHeaders());
	}

	private StompSession open(final StompHeaders stompHeaders)
			throws Exception {
		final var httpHeaders = new WebSocketHttpHeaders();
		final CompletableFuture<StompSession> future = stompClient.connectAsync(
				url, httpHeaders, stompHeaders, stompSessionHandler);

		final StompSession session = future.get(WAIT_TIMEOUT.toSeconds(),
				TimeUnit.SECONDS);
		assertThat(session.isConnected()).isTrue();

		sessions.add(session);
		if (stompSession == null) {
			stompSession = session;
		}

		return session;
	}

	protected FrameHandler subscribe(final StompSession session,
			final String destination) {
		final FrameHandler frameHandler = new FrameHandler();
		session.subscribe(destination, frameHandler);
		return frameHandler;
	}

	protected FrameHandler subscribe(final String destination) {
		return subscribe(stompSession, destination);
	}

	protected FrameHandler subscribeToRejections(final StompSession session) {
		return subscribe(session, "/user/queue/rejections");
	}

	protected FrameHandler subscribeToRejections() {
		return subscribeToRejections(stompSession);
	}

	protected FrameHandler subscribeToAcknowledgements(
			final StompSession session) {
		return subscribe(session, "/user/queue/acks");
	}

	protected FrameHandler subscribeToAcknowledgements() {
		return subscribeToAcknowledgements(stompSession);
	}

	protected void send(final StompSession session, final String destination,
			final UUID changeId, final Object dto) {
		final var stompHeaders = new StompHeaders();
		stompHeaders.setDestination(destination);
		stompHeaders.add("client-id", CLIENT_ID.toString());
		if (changeId != null) {
			stompHeaders.add("change-id", changeId.toString());
		}

		session.send(stompHeaders, dto);
	}

	protected void send(final String destination, final UUID changeId,
			final Object dto) {
		send(stompSession, destination, changeId, dto);
	}

	protected void send(final String destination, final Object dto) {
		send(destination, null, dto);
	}
}
