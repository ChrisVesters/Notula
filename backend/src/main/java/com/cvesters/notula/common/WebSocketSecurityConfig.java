package com.cvesters.notula.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.messaging.access.intercept.MessageAuthorizationContext;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;

@Configuration
@EnableWebSocketSecurity
public class WebSocketSecurityConfig {

	@Bean
	AuthorizationManager<Message<?>> messageAuthorizationManager(
			MessageMatcherDelegatingAuthorizationManager.Builder messages) {

		messages.simpSubscribeDestMatchers("/topic/tenant/**")
				.access(tenantAuthorizationManager())
				.anyMessage()
				.authenticated();

		return messages.build();
	}

	@Bean
	AuthorizationManager<MessageAuthorizationContext<?>> tenantAuthorizationManager() {
		return (authentication, context) -> {

			String destination = context.getMessage()
					.getHeaders()
					.get("simpDestination", String.class);

			if (destination == null) {
				return new AuthorizationDecision(false);
			}

			Authentication auth = authentication.get();

			return new AuthorizationDecision(true);
		};
	}
}
