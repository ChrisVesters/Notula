package com.cvesters.notula.test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContext;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import com.cvesters.notula.session.TestSession;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithSession.SecurityContextFactory.class)
public @interface WithSession {

	TestSession value();

	public static final class SecurityContextFactory
			implements WithSecurityContextFactory<WithSession> {

		@Override
		public SecurityContext createSecurityContext(
				final WithSession session) {
			final TestSession userSession = session.value();
			final var authToken = userSession.getAuthToken();

			final SecurityContext context = SecurityContextHolder
					.createEmptyContext();
			context.setAuthentication(authToken);
			return context;
		}
	}
}