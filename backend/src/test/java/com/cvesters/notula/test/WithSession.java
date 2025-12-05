package com.cvesters.notula.test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.test.context.support.WithSecurityContext;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import com.cvesters.notula.session.TestSession;
import com.cvesters.notula.user.TestUser;

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
			final TestUser user = userSession.getUser();

			final var now = Instant.now();
			final var iat = now;
			final var exp = now.plus(Duration.ofMinutes(15));

			final var headers = new HashMap<String, Object>();
			headers.put("alg", "none");

			final var claims = new HashMap<String, Object>();
			claims.put("sub", user.getId());

			final var jwt = new Jwt("token", iat, exp, headers, claims);

			final var authorities = new ArrayList<GrantedAuthority>();

			JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt,
					authorities);
			final SecurityContext context = SecurityContextHolder
					.createEmptyContext();
			context.setAuthentication(auth);
			return context;
		}
	}
}