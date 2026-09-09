package com.cvesters.notula.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.config.SimpleBrokerRegistration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

class WebSocketConfigTest {

	private static final String FRONTEND_URL = "https://localhost:4443";

	private final TaskScheduler scheduler = mock();
	private final ThreadPoolTaskExecutor inboundExecutor = mock();
	private final WebSocketChannelInterceptor channelInterceptor = mock();
	private final SessionOrderInterceptor sessionOrderInterceptor = mock();
	private final OriginArgumentResolver originResolver = mock();
	private final ChangeIdArgumentResolver changeIdResolver = mock();
	private final WebSocketSessionRegistry sessionRegistry = mock();

	private final WebSocketConfig config = new WebSocketConfig(scheduler,
			inboundExecutor, channelInterceptor, sessionOrderInterceptor,
			originResolver, changeIdResolver, sessionRegistry, FRONTEND_URL);

	@Nested
	class Broker {

		private final MessageBrokerRegistry registry = mock();
		private final SimpleBrokerRegistration broker = mock();

		@Test
		void destinations() {
			when(registry.enableSimpleBroker(any(String[].class)))
					.thenReturn(broker);
			when(broker.setHeartbeatValue(any())).thenReturn(broker);

			config.configureMessageBroker(registry);

			verify(registry).enableSimpleBroker("/topic", "/queue");
			verify(registry).setUserDestinationPrefix("/user");
			verify(registry).setApplicationDestinationPrefixes(
					WebSocketConfig.APPLICATION_PREFIX);
		}

		@Test
		void heartbeats() {
			when(registry.enableSimpleBroker(any(String[].class)))
					.thenReturn(broker);
			when(broker.setHeartbeatValue(any())).thenReturn(broker);

			config.configureMessageBroker(registry);

			final var heartbeat = ArgumentCaptor.forClass(long[].class);
			verify(broker).setHeartbeatValue(heartbeat.capture());

			assertThat(heartbeat.getValue()).containsExactly(10000L, 10000L);
			verify(broker).setTaskScheduler(scheduler);
		}
	}

	@Nested
	class Endpoint {

		@Test
		void success() {
			final StompEndpointRegistry registry = mock();
			final StompWebSocketEndpointRegistration registration = mock();
			when(registry.addEndpoint(any(String[].class)))
					.thenReturn(registration);

			config.registerStompEndpoints(registry);

			verify(registry).addEndpoint("/ws");
			verify(registration).setAllowedOrigins(FRONTEND_URL);
		}
	}

	@Nested
	class InboundChannel {

		@Test
		void success() {
			final ChannelRegistration registration = mock();

			config.configureClientInboundChannel(registration);

			verify(registration).interceptors(channelInterceptor,
					sessionOrderInterceptor);
			verify(registration).taskExecutor(inboundExecutor);
		}
	}

	@Nested
	class Transport {

		@Test
		void success() {
			final WebSocketTransportRegistration registration = mock();

			config.configureWebSocketTransport(registration);

			verify(registration).addDecoratorFactory(sessionRegistry);
		}
	}

	@Nested
	class ArgumentResolvers {

		@Test
		void success() {
			final List<HandlerMethodArgumentResolver> resolvers =
					new ArrayList<>();

			config.addArgumentResolvers(resolvers);

			assertThat(resolvers).containsExactly(originResolver,
					changeIdResolver);
		}
	}
}
