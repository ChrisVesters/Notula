package com.cvesters.notula.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;

class JwtConfigTest {

	private static final String SECRET = "a-secret-long-enough-for-hmac-sha-512-which-needs-sixty-four-bytes-of-key-material";
	private static final String OTHER_SECRET = "another-secret-long-enough-for-hmac-sha-512-and-different-from-the-first-one-here";

	private static final JwtConfig CONFIG = new JwtConfig(SECRET);

	private String token(final JwtConfig with) {
		final var claims = JwtClaimsSet.builder()
				.subject("42")
				.issuedAt(Instant.now())
				.expiresAt(Instant.now().plusSeconds(60))
				.build();
		final var header = JwsHeader.with(JwtConfig.MAC_ALGORITHM).build();

		return with.jwtEncoder()
				.encode(JwtEncoderParameters.from(header, claims))
				.getTokenValue();
	}

	@Nested
	class Decoder {

		@Test
		void success() {
			final Jwt decoded = CONFIG.jwtDecoder().decode(token(CONFIG));

			assertThat(decoded.getSubject()).isEqualTo("42");
			assertThat(decoded.getHeaders().get("alg"))
					.hasToString(JwtConfig.MAC_ALGORITHM.getName());
		}

		@Test
		void otherKey() {
			final String signedElsewhere = token(new JwtConfig(OTHER_SECRET));

			assertThatThrownBy(
					() -> CONFIG.jwtDecoder().decode(signedElsewhere))
					.isInstanceOf(JwtException.class);
		}

		@Test
		void malformed() {
			assertThatThrownBy(() -> CONFIG.jwtDecoder().decode("not-a-token"))
					.isInstanceOf(JwtException.class);
		}
	}
}
