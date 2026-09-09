package com.cvesters.notula.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.simp.annotation.support.SimpAnnotationMethodMessageHandler;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;

class SessionOrderInterceptorTest {

	private static final String SESSION_ID = "session";
	private static final String ACTION = "/app/meetings/1/changes";
	private static final String BROADCAST = "/topic/meetings/1";

	private final SessionOrder order = mock();
	private final SessionOrderInterceptor interceptor =
			new SessionOrderInterceptor(order);

	private final MessageChannel channel = mock();

	private static Message<String> message(final StompCommand command,
			final String sessionId, final String destination) {
		final var accessor = StompHeaderAccessor.create(command);
		accessor.setSessionId(sessionId);
		accessor.setDestination(destination);

		return MessageBuilder.createMessage("", accessor.getMessageHeaders());
	}

	private static Message<String> action() {
		return message(StompCommand.SEND, SESSION_ID, ACTION);
	}

	@Nested
	class PreSend {

		@Test
		void success() {
			final Message<?> message = action();

			final Message<?> result = interceptor.preSend(message, channel);

			verify(order).acquire(SESSION_ID);
			assertThat(result).isEqualTo(message);
		}

		@Test
		void noSessionId() {
			final Message<?> message = message(StompCommand.SEND, null,
					ACTION);

			final Message<?> result = interceptor.preSend(message, channel);

			assertThat(result).isEqualTo(message);
			verifyNoInteractions(order);
		}

		@Test
		void subscribe() {
			final Message<?> message = message(StompCommand.SUBSCRIBE,
					SESSION_ID, BROADCAST);

			final Message<?> result = interceptor.preSend(message, channel);

			assertThat(result).isEqualTo(message);
			verifyNoInteractions(order);
		}

		@Test
		void broadcast() {
			final Message<?> message = message(StompCommand.SEND, SESSION_ID,
					BROADCAST);

			final Message<?> result = interceptor.preSend(message, channel);

			assertThat(result).isEqualTo(message);
			verifyNoInteractions(order);
		}

		@Test
		void noDestination() {
			final Message<?> message = message(StompCommand.SEND, SESSION_ID,
					null);

			final Message<?> result = interceptor.preSend(message, channel);

			assertThat(result).isEqualTo(message);
			verifyNoInteractions(order);
		}

		@Test
		void disconnect() {
			final Message<?> message = message(StompCommand.DISCONNECT,
					SESSION_ID, null);

			final Message<?> result = interceptor.preSend(message, channel);

			assertThat(result).isEqualTo(message);
			verify(order).forget(SESSION_ID);
		}
	}

	@Nested
	class AfterMessageHandled {

		private final SimpAnnotationMethodMessageHandler actions = mock();

		@Test
		void success() {
			interceptor.afterMessageHandled(action(), channel, actions, null);

			verify(order).release(SESSION_ID);
		}

		@Test
		void failed() {
			interceptor.afterMessageHandled(action(), channel, actions,
					new IllegalStateException());

			verify(order).release(SESSION_ID);
		}

		@Test
		void otherHandler() {
			final MessageHandler broker = mock();

			interceptor.afterMessageHandled(action(), channel, broker, null);

			verifyNoInteractions(order);
		}

		@Test
		void broadcast() {
			interceptor.afterMessageHandled(
					message(StompCommand.SEND, SESSION_ID, BROADCAST), channel,
					actions, null);

			verifyNoInteractions(order);
		}

		@Test
		void noSessionId() {
			interceptor.afterMessageHandled(
					message(StompCommand.SEND, null, ACTION), channel, actions,
					null);

			verifyNoInteractions(order);
		}
	}
}
