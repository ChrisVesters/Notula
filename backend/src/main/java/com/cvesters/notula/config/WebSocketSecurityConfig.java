package com.cvesters.notula.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;

@Configuration
@EnableWebSocketSecurity
public class WebSocketSecurityConfig {

	private final List<WebSocketAuthManager> authManagers;

	public WebSocketSecurityConfig(
			final List<WebSocketAuthManager> authManagers) {
		this.authManagers = authManagers;
	}

	@Bean
	public ChannelInterceptor csrfChannelInterceptor() {
		// disabling csrf
		return new ChannelInterceptor() {
		};
	}

	@Bean
	AuthorizationManager<Message<?>> messageAuthorizationManager(
			MessageMatcherDelegatingAuthorizationManager.Builder messages) {

		messages.simpTypeMatchers(SimpMessageType.CONNECT).permitAll();
		messages.simpTypeMatchers(SimpMessageType.HEARTBEAT).permitAll();
		messages.simpTypeMatchers(SimpMessageType.DISCONNECT).permitAll();
		// TODO: not only subscribe!!! also send
		authManagers.forEach(authManager -> {
			messages.simpDestMatchers(authManager.getPattern())
					.access(authManager);
		});
		messages.anyMessage().authenticated();

		return messages.build();
	}
}
