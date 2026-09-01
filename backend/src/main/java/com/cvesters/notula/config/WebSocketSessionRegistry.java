package com.cvesters.notula.config;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;
import org.springframework.web.socket.handler.WebSocketHandlerDecoratorFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WebSocketSessionRegistry
		implements WebSocketHandlerDecoratorFactory {

	private final TaskScheduler scheduler;

	public WebSocketSessionRegistry(
			final TaskScheduler webSocketTaskScheduler) {
		this.scheduler = webSocketTaskScheduler;
	}

	@Override
	public WebSocketHandler decorate(final WebSocketHandler handler) {
		return new SessionTracker(handler);
	}

	private final class SessionTracker extends WebSocketHandlerDecorator {

		private static final CloseStatus EXPIRED = CloseStatus.POLICY_VIOLATION
				.withReason("Access token expired");

		private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

		private SessionTracker(final WebSocketHandler delegate) {
			super(delegate);
		}

		@Override
		public void handleMessage(final WebSocketSession session,
				final WebSocketMessage<?> message) throws Exception {
			super.handleMessage(session, message);
			register(session);
		}

		@Override
		public void afterConnectionClosed(final WebSocketSession session,
				final CloseStatus status) throws Exception {
			sessions.remove(session.getId());
			super.afterConnectionClosed(session, status);
		}

		private void register(final WebSocketSession session) {
			final Object attribute = session.getAttributes()
					.get(SessionAttributes.EXPIRES_AT.name());
			if (!(attribute instanceof final Instant expiresAt)) {
				return;
			}

			if (sessions.putIfAbsent(session.getId(), session) != null) {
				return;
			}

			scheduler.schedule(() -> close(session.getId()), expiresAt);
		}

		private void close(final String sessionId) {
			final WebSocketSession session = sessions.remove(sessionId);
			if (session == null || !session.isOpen()) {
				return;
			}

			try {
				log.info("Closing WebSocket session {}: access token expired",
						sessionId);
				session.close(EXPIRED);
			} catch (final IOException e) {
				log.warn("Unable to close WebSocket session {}", sessionId, e);
			}
		}
	}
}
