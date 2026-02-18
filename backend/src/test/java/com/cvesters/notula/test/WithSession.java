package com.cvesters.notula.test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContext;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import com.cvesters.notula.config.JwtAuthConverter;
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

			final var authorities = new ArrayList<GrantedAuthority>();
			if (userSession.getOrganisation() != null) {
				authorities
						.add(new SimpleGrantedAuthority("CLAIM_ORGANISATION"));
			}

			final var authToken = new JwtAuthConverter.AuthToken(userSession.principal(), authorities);
			final SecurityContext context = SecurityContextHolder
					.createEmptyContext();
			context.setAuthentication(authToken);
			return context;
		}
	}
}