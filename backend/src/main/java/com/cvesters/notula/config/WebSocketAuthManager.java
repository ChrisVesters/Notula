package com.cvesters.notula.config;

import java.util.Objects;
import java.util.function.Supplier;

import org.apache.commons.lang3.Validate;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.messaging.access.intercept.MessageAuthorizationContext;

import lombok.Getter;

import com.cvesters.notula.common.domain.Principal;

@Getter
public abstract class WebSocketAuthManager
		implements AuthorizationManager<MessageAuthorizationContext<?>> {

	private final String pattern;

	public WebSocketAuthManager(final String pattern) {
		Validate.notBlank(pattern);

		this.pattern = pattern;
	}

	@Override
	public final AuthorizationResult authorize(
			final Supplier<? extends Authentication> authentication,
			final MessageAuthorizationContext<?> context) {
		Objects.requireNonNull(authentication);
		Objects.requireNonNull(context);

		final Authentication auth = authentication.get();
		if (auth == null) {
			return null;
		}
		if (auth.getPrincipal() instanceof Principal principal) {
			return new AuthorizationDecision(hasAccess(principal, context));
		}

		return null;
	}

	public abstract boolean hasAccess(final Principal principal,
			final MessageAuthorizationContext<?> context);

}
