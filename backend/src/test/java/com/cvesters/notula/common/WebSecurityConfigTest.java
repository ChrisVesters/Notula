package com.cvesters.notula.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.HashMap;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import com.cvesters.notula.session.TestSession;

class WebSecurityConfigTest {

	private final WebSecurityConfig config = new WebSecurityConfig();

	@Nested
	class JwtConverter {

		private final JwtAuthenticationConverter converter = config
				.jwtAuthenticationConverter();

		@Test
		void withoutOrganisation() {
			final var jwt = toJwt(TestSession.ALISON_DACH);

			final var token = converter.convert(jwt);

			assertThat(token.isAuthenticated()).isTrue();
			assertThat(token.getPrincipal()).isEqualTo(jwt);
			assertThat(token.getAuthorities()).isEmpty();
		}

		@Test
		void withOrganisation() {
			final var jwt = toJwt(TestSession.EDUARDO_CHRISTIANSEN_SPORER);

			final var token = converter.convert(jwt);

			assertThat(token.isAuthenticated()).isTrue();
			assertThat(token.getPrincipal()).isEqualTo(jwt);
			assertThat(token.getAuthorities()).satisfiesExactlyInAnyOrder(
					authority -> assertThat(authority.getAuthority())
							.isEqualTo("CLAIM_ORGANISATION"));
		}

		private Jwt toJwt(final TestSession session) {
			final var iat = Instant.now();
			final var exp = session.getActiveUntil().toInstant();

			final var headers = new HashMap<String, Object>();
			headers.put("alg", "none");

			final var claims = new HashMap<String, Object>();
			claims.put("sub", session.getUser().getId());

			if (session.getOrganisation() != null) {
				claims.put("organisation_id",
						session.getOrganisation().getId());
			}

			return new Jwt("token", iat, exp, headers, claims);
		}
	}
}
