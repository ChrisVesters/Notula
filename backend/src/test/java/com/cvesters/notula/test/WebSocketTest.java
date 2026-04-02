package com.cvesters.notula.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.converter.CompositeMessageConverter;
import org.springframework.messaging.converter.JacksonJsonMessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;
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

	@MockitoBean
	private JwtDecoder jwtDecoder;

	@MockitoBean
	private JwtAuthConverter authManager;

	@LocalServerPort
	private int port;
	private String url;

	private WebSocketStompClient stompClient;
	private StompSession stompSession;

	protected final SessionHandler stompSessionHandler = new SessionHandler();

	@BeforeEach
	void setup() {
		final var webSocketClient = new StandardWebSocketClient();
		stompClient = new WebSocketStompClient(webSocketClient);
		stompClient.setMessageConverter(new CompositeMessageConverter(
				List.of(new JacksonJsonMessageConverter(),
						new StringMessageConverter())));

		url = "ws://localhost:" + port + "/ws";
	}

	@AfterEach
	void teardown() {
		if (stompSession != null && stompSession.isConnected()) {
			stompSession.disconnect();
		}
	}

	protected void connect(final TestSession session) throws Exception {
		final String token = "token";
		final Jwt jwt = mock();
		when(jwtDecoder.decode(token)).thenReturn(jwt);

		final var authToken = session.getAuthToken();
		when(authManager.convert(jwt)).thenReturn(authToken);

		final var httpHeaders = new WebSocketHttpHeaders();
		final var stompHeaders = new StompHeaders();
		stompHeaders.add("Authorization", "Bearer " + token);
		final CompletableFuture<StompSession> future = stompClient.connectAsync(
				url, httpHeaders, stompHeaders, stompSessionHandler);

		stompSession = future.get(WAIT_TIMEOUT.toSeconds(), TimeUnit.SECONDS);
		assertThat(stompSession.isConnected()).isTrue();
	}

	protected void connect() throws Exception {
		final var httpHeaders = new WebSocketHttpHeaders();
		final var stompHeaders = new StompHeaders();
		final CompletableFuture<StompSession> future = stompClient
				.connectAsync(url, httpHeaders, stompHeaders, stompSessionHandler);

		stompSession = future.get(WAIT_TIMEOUT.toSeconds(), TimeUnit.SECONDS);
		assertThat(stompSession.isConnected()).isTrue();
	}

	protected <T> FrameHandler<T> subscribe(final String destination,
			final Class<T> clazz) {
		final FrameHandler<T> frameHandler = new FrameHandler<>(clazz);
		stompSession.subscribe(destination, frameHandler);
		return frameHandler;
	}

	protected FrameHandler<String> subscribeToErrors() {
		return subscribe("/user/queue/errors", String.class);
	}

	protected void send(final String destination, final Object dto) {
		stompSession.send(destination, dto);
	}
}
