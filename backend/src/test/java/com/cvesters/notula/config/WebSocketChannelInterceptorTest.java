package com.cvesters.notula.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import com.cvesters.notula.config.JwtAuthConverter.AuthToken;

class WebSocketChannelInterceptorTest {

	private final JwtDecoder decoder = mock();
	private final JwtAuthConverter converter = mock();

	private final WebSocketChannelInterceptor interceptor = new WebSocketChannelInterceptor(
			decoder, converter);

	@Nested
	class PreSend {

		private final MessageChannel channel = mock();

		@Test
		void noStompHeader() {
			final var accessor = StompHeaderAccessor.create();

			final Message<String> message = MessageBuilder.createMessage("",
					accessor.getMessageHeaders());

			final Message<?> result = interceptor.preSend(message, channel);

			assertThat(result).isEqualTo(message);
			assertThat(result.getHeaders().get("simpUser")).isNull();
		}

		@Test
		void nonConnectCommand() {
			final var accessor = StompHeaderAccessor.create(StompCommand.ABORT);

			final Message<String> message = MessageBuilder.createMessage("",
					accessor.getMessageHeaders());

			final Message<?> result = interceptor.preSend(message, channel);

			assertThat(result).isEqualTo(message);
			assertThat(result.getHeaders().get("simpUser")).isNull();
		}

		@Test
		void noAuthorizationHeader() {
			final var accessor = StompHeaderAccessor
					.create(StompCommand.CONNECT);

			final Message<String> message = MessageBuilder.createMessage("",
					accessor.getMessageHeaders());

			final Message<?> result = interceptor.preSend(message, channel);

			assertThat(result).isEqualTo(message);
			assertThat(result.getHeaders().get("simpUser")).isNull();
		}

		@Test
		void nonBearerAuthorizationHeader() {
			final var accessor = StompHeaderAccessor
					.create(StompCommand.CONNECT);
			accessor.addNativeHeader("Authorization", "admin");

			final Message<String> message = MessageBuilder.createMessage("",
					accessor.getMessageHeaders());

			final Message<?> result = interceptor.preSend(message, channel);

			assertThat(result).isEqualTo(message);
			assertThat(result.getHeaders().get("simpUser")).isNull();
		}

		@Test
		void noJwtExpiry() {
			final var accessor = StompHeaderAccessor
					.create(StompCommand.CONNECT);
			accessor.addNativeHeader("Authorization", "Bearer token");
			accessor.setLeaveMutable(true);

			final Map<String, Object> attributes = new HashMap<>();
			accessor.setSessionAttributes(attributes);

			final Message<String> message = MessageBuilder.createMessage("",
					accessor.getMessageHeaders());

			final Jwt jwtToken = mock();
			when(decoder.decode("token")).thenReturn(jwtToken);
			when(jwtToken.getExpiresAt()).thenReturn(null);

			final Message<?> result = interceptor.preSend(message, channel);

			assertThat(result).isEqualTo(message);
			assertThat(result.getHeaders().get("simpUser")).isNull();
		}

		@Test
		void noSessionAttributes() {
			final var accessor = StompHeaderAccessor
					.create(StompCommand.CONNECT);
			accessor.addNativeHeader("Authorization", "Bearer token");
			accessor.setLeaveMutable(true);

			final Message<String> message = MessageBuilder.createMessage("",
					accessor.getMessageHeaders());

			final var expiresAt = Instant.now().plusSeconds(60);
			final Jwt jwtToken = mock();
			when(decoder.decode("token")).thenReturn(jwtToken);
			when(jwtToken.getExpiresAt()).thenReturn(expiresAt);

			final Message<?> result = interceptor.preSend(message, channel);

			assertThat(result).isEqualTo(message);
			assertThat(result.getHeaders().get("simpUser")).isNull();
		}

		@Test
		void withAuthorizationHeader() {
			final var accessor = StompHeaderAccessor
					.create(StompCommand.CONNECT);
			accessor.addNativeHeader("Authorization", "Bearer token");
			accessor.setLeaveMutable(true);

			final Map<String, Object> attributes = new HashMap<>();
			accessor.setSessionAttributes(attributes);

			final Message<String> message = MessageBuilder.createMessage("",
					accessor.getMessageHeaders());

			final var expiresAt = Instant.now().plusSeconds(60);
			final Jwt jwtToken = mock();
			when(decoder.decode("token")).thenReturn(jwtToken);
			when(jwtToken.getExpiresAt()).thenReturn(expiresAt);

			final AuthToken authToken = mock();
			when(converter.convert(jwtToken)).thenReturn(authToken);

			final Message<?> result = interceptor.preSend(message, channel);

			assertThat(result).isEqualTo(message);
			assertThat(result.getHeaders()).isNotEmpty();
			assertThat(result.getHeaders()).containsEntry("simpUser",
					authToken);
			assertThat(attributes).containsEntry(
					SessionAttributes.EXPIRES_AT.name(), expiresAt);
		}
	}
}
