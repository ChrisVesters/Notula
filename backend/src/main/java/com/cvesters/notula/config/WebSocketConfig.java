package com.cvesters.notula.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@Configuration
@EnableWebSocketMessageBroker
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	private final TaskScheduler scheduler;
	private final WebSocketChannelInterceptor channelInterceptor;
	private final OriginArgumentResolver originArgumentResolver;
	private final WebSocketSessionRegistry sessionRegistry;
	private final String frontendUrl;

	public WebSocketConfig(final TaskScheduler webSocketTaskScheduler,
			final WebSocketChannelInterceptor interceptor,
			final OriginArgumentResolver originArgumentResolver,
			final WebSocketSessionRegistry sessionRegistry,
			@Value("${frontend.url}") final String frontendUrl) {
		this.scheduler = webSocketTaskScheduler;
		this.channelInterceptor = interceptor;
		this.originArgumentResolver = originArgumentResolver;
		this.sessionRegistry = sessionRegistry;
		this.frontendUrl = frontendUrl;
	}

	@Override
	public void configureMessageBroker(final MessageBrokerRegistry config) {
		config.enableSimpleBroker("/topic", "/queue")
				.setHeartbeatValue(new long[] { 10000, 10000 })
				.setTaskScheduler(scheduler);
		config.setUserDestinationPrefix("/user");
		config.setApplicationDestinationPrefixes("/app");
	}

	@Override
	public void registerStompEndpoints(final StompEndpointRegistry registry) {
		registry.addEndpoint("/ws").setAllowedOrigins(frontendUrl);
	}

	@Override
	public void configureClientInboundChannel(
			final ChannelRegistration registration) {
		registration.interceptors(channelInterceptor);
	}

	@Override
	public void configureWebSocketTransport(
			final WebSocketTransportRegistration registration) {
		registration.addDecoratorFactory(sessionRegistry);
	}

	@Override
	public void addArgumentResolvers(
			final List<HandlerMethodArgumentResolver> argumentResolvers) {
		argumentResolvers.add(originArgumentResolver);
	}
}
