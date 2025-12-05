package com.cvesters.notula.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;

import com.cvesters.notula.session.bdo.SessionInfo;

class AccessTokenServiceTest {

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_DEKSTOP;

	private final JwtEncoder jwtEncoder = mock();

	private final AccessTokenService accessTokenService = new AccessTokenService(
			jwtEncoder);

	@Nested
	class Create {

		@Test
		void success() throws Exception {
			final SessionInfo session = SESSION.info();

			final var now = Instant.now();

			final Jwt token = mock();
			final String tokenValue = "JWT";
			when((token).getTokenValue()).thenReturn(tokenValue);

			when(jwtEncoder.encode(argThat(params -> {
				final var header = params.getJwsHeader();
				assertThat(header.getAlgorithm().getName()).isEqualTo("HS512");

				final var claims = params.getClaims();
				assertThat(claims.getSubject())
						.isEqualTo(String.valueOf(SESSION.getUser().getId()));
				assertThat(claims.getIssuedAt()).isCloseTo(now,
						within(Duration.ofSeconds(1)));
				assertThat(claims.getExpiresAt()).isCloseTo(
						now.plus(Duration.ofMinutes(30)),
						within(Duration.ofSeconds(1)));
				return true;
			}))).thenReturn(token);

			final String result = accessTokenService.create(session);

			assertThat(result).isEqualTo(tokenValue);
		}

		@Test
		void sessionNull() {
			assertThatThrownBy(() -> accessTokenService.create(null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
