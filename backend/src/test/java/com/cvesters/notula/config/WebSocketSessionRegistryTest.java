package com.cvesters.notula.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

class WebSocketSessionRegistryTest {

	private static final String SESSION_ID = "session-1";

	private final TaskScheduler scheduler = mock();

	private final WebSocketSessionRegistry registry = new WebSocketSessionRegistry(
			scheduler);

	private final WebSocketHandler delegate = mock();
	private final WebSocketHandler handler = registry.decorate(delegate);

	@Nested
	class HandleMessage {

		@Test
		void delegates() throws Exception {
			final WebSocketSession session = session(true);
			final WebSocketMessage<?> message = mock();

			handler.handleMessage(session, message);

			verify(delegate).handleMessage(session, message);
		}

		@Test
		void authenticatedSessionIsScheduled() throws Exception {
			final var expiresAt = Instant.now().plusSeconds(60);
			final WebSocketSession session = session(true, expiresAt);

			handler.handleMessage(session, mock());

			final var scheduled = ArgumentCaptor.forClass(Instant.class);
			verify(scheduler).schedule(any(Runnable.class),
					scheduled.capture());

			assertThat(scheduled.getValue()).isEqualTo(expiresAt);
		}

		@Test
		void unauthenticatedSessionIsNotScheduled() throws Exception {
			handler.handleMessage(session(true), mock());

			verify(scheduler, never()).schedule(any(Runnable.class),
					any(Instant.class));
		}

		@Test
		void laterTokenIsIgnored() throws Exception {
			final var expiresAt = Instant.now().plusSeconds(60);
			final WebSocketSession session = session(true, expiresAt);

			handler.handleMessage(session, mock());

			session.getAttributes()
					.put(SessionAttributes.EXPIRES_AT.name(),
							Instant.now().plusSeconds(3600));
			handler.handleMessage(session, mock());

			final var scheduled = ArgumentCaptor.forClass(Instant.class);
			verify(scheduler).schedule(any(Runnable.class),
					scheduled.capture());

			assertThat(scheduled.getValue()).isEqualTo(expiresAt);
		}
	}

	@Nested
	class Close {

		@Test
		void closesSessionWhenDue() throws Exception {
			final WebSocketSession session = session(true,
					Instant.now().plusSeconds(60));
			handler.handleMessage(session, mock());

			verify(session, never()).close(any());

			runScheduled();

			verify(session).close(CloseStatus.POLICY_VIOLATION
					.withReason("Access token expired"));
		}

		@Test
		void alreadyClosedSession() throws Exception {
			final WebSocketSession session = session(false,
					Instant.now().plusSeconds(60));
			handler.handleMessage(session, mock());

			runScheduled();

			verify(session, never()).close(any());
		}

		@Test
		void sessionClosedBeforeItsTokenExpired() throws Exception {
			final WebSocketSession session = session(true,
					Instant.now().plusSeconds(60));
			handler.handleMessage(session, mock());

			handler.afterConnectionClosed(session, CloseStatus.NORMAL);
			runScheduled();

			verify(session, never()).close(any());
		}

		@Test
		void reconnectedSessionIsScheduledAgain() throws Exception {
			final WebSocketSession session = session(true,
					Instant.now().plusSeconds(60));

			handler.handleMessage(session, mock());
			handler.afterConnectionClosed(session, CloseStatus.NORMAL);
			handler.handleMessage(session, mock());

			verify(scheduler, times(2)).schedule(any(Runnable.class),
					any(Instant.class));
		}

		@Test
		void closeFails() throws Exception {
			final WebSocketSession session = session(true,
					Instant.now().plusSeconds(60));
			handler.handleMessage(session, mock());

			doThrow(IOException.class).when(session).close();

			verify(session, never()).close(any());

			assertThatNoException().isThrownBy(() -> runScheduled());

			verify(session).close(CloseStatus.POLICY_VIOLATION
					.withReason("Access token expired"));
		}
	}

	private WebSocketSession session(final boolean open) {
		return session(open, null);
	}

	private WebSocketSession session(final boolean open,
			final Instant expiresAt) {
		final Map<String, Object> attributes = new HashMap<>();
		if (expiresAt != null) {
			attributes.put(SessionAttributes.EXPIRES_AT.name(), expiresAt);
		}

		final WebSocketSession session = mock();
		when(session.getId()).thenReturn(SESSION_ID);
		when(session.isOpen()).thenReturn(open);
		when(session.getAttributes()).thenReturn(attributes);

		return session;
	}

	private void runScheduled() {
		final var task = ArgumentCaptor.forClass(Runnable.class);
		verify(scheduler).schedule(task.capture(), any(Instant.class));

		assertThat(task.getValue()).isNotNull();
		task.getValue().run();
	}
}
