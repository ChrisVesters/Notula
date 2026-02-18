package com.cvesters.notula.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.HashMap;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import com.cvesters.notula.session.TestSession;

class JwtAuthConverterTest {

	private final JwtAuthConverter converter = new JwtAuthConverter();

	@Test
	void withoutOrganisation() {
		final var session = TestSession.ALISON_DACH;
		final var jwt = toJwt(session);

		final var token = converter.convert(jwt);

		assertThat(token.isAuthenticated()).isTrue();
		assertThat(token.getPrincipal()).isEqualTo(session.principal());
		assertThat(token.getAuthorities())
				.extracting(GrantedAuthority::getAuthority)
				.isEmpty();
	}

	@Test
	void withOrganisation() {
		final var session = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
		final var jwt = toJwt(session);

		final var token = converter.convert(jwt);

		assertThat(token.isAuthenticated()).isTrue();
		assertThat(token.getPrincipal()).isEqualTo(session.principal());
		assertThat(token.getAuthorities())
				.extracting(GrantedAuthority::getAuthority)
				.containsOnly("CLAIM_ORGANISATION");
	}

	private Jwt toJwt(final TestSession session) {
		final var iat = Instant.now();
		final var exp = session.getActiveUntil().toInstant();

		final var headers = new HashMap<String, Object>();
		headers.put("alg", "none");

		final var claims = new HashMap<String, Object>();
		claims.put("sub", session.getUser().getId());

		if (session.getOrganisation() != null) {
			claims.put("organisation_id", session.getOrganisation().getId());
		}

		return new Jwt("token", iat, exp, headers, claims);
	}
}
