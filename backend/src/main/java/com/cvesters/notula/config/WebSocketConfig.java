package com.cvesters.notula.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	private final WebSocketChannelInterceptor channelInterceptor;

	public WebSocketConfig(final WebSocketChannelInterceptor interceptor) {
		this.channelInterceptor = interceptor;
	}

	@Override
	public void configureMessageBroker(final MessageBrokerRegistry config) {
		config.enableSimpleBroker("/topic");
		config.setApplicationDestinationPrefixes("/app");
	}

	@Override
	public void registerStompEndpoints(final StompEndpointRegistry registry) {
		// TODO: restrict
		registry.addEndpoint("/ws").setAllowedOriginPatterns("*");
		// registry.addEndpoint("/chat").withSockJS();
	}

	@Override
	public void configureClientInboundChannel(
			final ChannelRegistration registration) {
		registration.interceptors(channelInterceptor);
	}
}
