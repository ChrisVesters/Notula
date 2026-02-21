package com.cvesters.notula.config;

import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

@Component
public class WebSocketChannelInterceptor implements ChannelInterceptor {

	private final JwtDecoder decoder;
	private final JwtAuthConverter authenticationConverter;

	public WebSocketChannelInterceptor(final JwtDecoder decoder,
			final JwtAuthConverter authenticationConverter) {
		this.decoder = decoder;
		this.authenticationConverter = authenticationConverter;
	}

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		final var accessor = MessageHeaderAccessor.getAccessor(message,
				StompHeaderAccessor.class);
		if (accessor == null || StompCommand.CONNECT != accessor.getCommand()) {
			return message;
		}

		final String authHeader = accessor
				.getFirstNativeHeader(HttpHeaders.AUTHORIZATION);
		final String bearerPrefix = OAuth2AccessToken.TokenType.BEARER
				.getValue();
		if (authHeader == null || !authHeader.startsWith(bearerPrefix)) {
			return message;
		}

		final String token = authHeader.substring(bearerPrefix.length() + 1);
		final Jwt jwt = decoder.decode(token);

		final var auth = authenticationConverter.convert(jwt);
		accessor.setUser(auth);
		return message;
	}
}
