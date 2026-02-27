package com.cvesters.notula.config;

import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.messaging.access.intercept.MessageAuthorizationContext;

import com.cvesters.notula.common.domain.Principal;

public abstract class WebSocketAuthManager
		implements AuthorizationManager<MessageAuthorizationContext<?>> {

	private final String pattern;

	public WebSocketAuthManager(final String pattern) {
		this.pattern = pattern;
	}

	@Override
	public final AuthorizationResult authorize(
			final Supplier<? extends @Nullable Authentication> authentication,
			final MessageAuthorizationContext<?> context) {

		final Authentication auth = authentication.get();
		if (auth.getPrincipal() instanceof Principal principal) {
			return new AuthorizationDecision(hasAccess(principal, context));
		}

		return null;
	}

	public String getPattern() {
		return pattern;
	}

	public abstract boolean hasAccess(final Principal principal,
			final  MessageAuthorizationContext<?> context);

}
