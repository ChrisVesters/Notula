package com.cvesters.notula.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.messaging.access.intercept.MessageAuthorizationContext;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager.Builder;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager.Builder.Constraint;

import com.cvesters.notula.common.domain.Principal;

class WebSocketSecurityConfigTest {

	private static final String PATTERN = "/topic/meetings/{id}";

	private final WebSocketAuthManager authManager = new WebSocketAuthManager(
			PATTERN) {

		@Override
		protected boolean hasAccess(final Principal principal,
				final MessageAuthorizationContext<?> context) {
			return true;
		}
	};

	private final WebSocketSecurityConfig config = new WebSocketSecurityConfig(
			List.of(authManager));

	@Nested
	class CsrfInterceptor {

		@Test
		void success() {
			final var interceptor = config.csrfChannelInterceptor();
			final Message<String> message = MessageBuilder
					.withPayload("payload")
					.build();

			assertThat(interceptor.preSend(message, mock())).isSameAs(message);
		}
	}

	@Nested
	class AuthorizationManagerBean {

		private final Builder messages = mock();

		private final Constraint constraint = mock();

		private final MessageMatcherDelegatingAuthorizationManager built =
				mock();

		@BeforeEach
		void stub() {
			when(messages.simpTypeMatchers(any())).thenReturn(constraint);
			when(messages.simpSubscribeDestMatchers(any()))
					.thenReturn(constraint);
			when(messages.anyMessage()).thenReturn(constraint);
			when(messages.build()).thenReturn(built);
		}

		@Test
		void success() {
			assertThat(config.messageAuthorizationManager(messages))
					.isSameAs(built);
		}

		@Test
		void permitted() {
			config.messageAuthorizationManager(messages);

			verify(messages).simpTypeMatchers(SimpMessageType.CONNECT);
			verify(messages).simpTypeMatchers(SimpMessageType.HEARTBEAT);
			verify(messages).simpTypeMatchers(SimpMessageType.DISCONNECT);
			verify(constraint, times(3)).permitAll();
		}

		@Test
		void subscriptions() {
			config.messageAuthorizationManager(messages);

			verify(messages).simpSubscribeDestMatchers(PATTERN);
			verify(constraint).access(authManager);
		}

		@Test
		void anythingElse() {
			config.messageAuthorizationManager(messages);

			verify(messages).anyMessage();
			verify(constraint).authenticated();
		}
	}
}
