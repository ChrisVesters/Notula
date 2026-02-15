package com.cvesters.notula.common;

import java.security.Principal;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	private final JwtDecoder jwtDecoder;

	public WebSocketConfig(final JwtDecoder jwtDecoder) {
		this.jwtDecoder = jwtDecoder;
	}

	@Override
	public void configureMessageBroker(final MessageBrokerRegistry config) {
		config.enableSimpleBroker("/topic");
		config.setApplicationDestinationPrefixes("/app");
	}

	@Override
	public void registerStompEndpoints(final StompEndpointRegistry registry) {
		registry.addEndpoint("/ws").setAllowedOriginPatterns("*");
		// registry.addEndpoint("/chat").withSockJS();
	}

	@Override
	public void configureClientInboundChannel(
			final ChannelRegistration registration) {
		// TODO: extract class
		registration.interceptors(new ChannelInterceptor() {

			@Override
			public Message<?> preSend(Message<?> message,
					MessageChannel channel) {
				StompHeaderAccessor accessor = MessageHeaderAccessor
						.getAccessor(message, StompHeaderAccessor.class);
				if (accessor == null) {
					return message;
				}

				System.out.println(accessor.getCommand());
				if (StompCommand.CONNECT != accessor.getCommand()) {
					return message;
				}

				final String authHeader = accessor
						.getFirstNativeHeader(HttpHeaders.AUTHORIZATION);
				if (authHeader != null && authHeader.startsWith("Bearer ")) {
					String token = authHeader.substring(7);
					final Jwt jwt = jwtDecoder.decode(token);
					final Principal auth = new JwtAuthenticationToken(jwt);
					accessor.setUser(auth);
				}

				return message;
			}
		});
	}

}

// @Configuration
// @EnableWebSocketMessageBroker
// public class WebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer
// {

// @Override
// public void configureMessageBroker(MessageBrokerRegistry config) {
// config.enableSimpleBroker("/topic");
// config.setApplicationDestinationPrefixes("/app");
// }

// @Override
// public void registerStompEndpoints(StompEndpointRegistry registry) {
// registry.addEndpoint("/chat");
// registry.addEndpoint("/chat").withSockJS();
// }
// }

// package com.example.messagingstompwebsocket;

// import org.springframework.context.annotation.Configuration;
// import org.springframework.messaging.simp.config.MessageBrokerRegistry;
// import
// org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
// import
// org.springframework.web.socket.config.annotation.StompEndpointRegistry;
// import
// org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

// @Configuration
// @EnableWebSocketMessageBroker
// public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

// @Override
// public void configureMessageBroker(MessageBrokerRegistry config) {
// config.enableSimpleBroker("/topic");
// config.setApplicationDestinationPrefixes("/app");
// }

// @Override
// public void registerStompEndpoints(StompEndpointRegistry registry) {
// registry.addEndpoint("/gs-guide-websocket");
// }

// }