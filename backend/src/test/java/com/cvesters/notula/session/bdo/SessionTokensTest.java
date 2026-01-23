package com.cvesters.notula.session.bdo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.session.TestSession;

class SessionTokensTest {

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
	private static final String ACCESS_TOKEN = "access_token";
	private static final String REFRESH_TOKEN = SESSION.getRefreshToken();

	@Nested
	class Constructor {

		@Test
		void full() {
			final var tokens = new SessionTokens(SESSION.info(), ACCESS_TOKEN,
					REFRESH_TOKEN);

			assertThat(tokens.getId()).isEqualTo(SESSION.getId());
			assertThat(tokens.getAccessToken()).isEqualTo(ACCESS_TOKEN);
			assertThat(tokens.getRefreshToken())
					.contains(SESSION.getRefreshToken());
			assertThat(tokens.getActiveUntil())
					.isEqualTo(SESSION.getActiveUntil());
		}

		@Test
		void withoutRefreshToken() {
			final var tokens = new SessionTokens(SESSION.info(), ACCESS_TOKEN);

			assertThat(tokens.getId()).isEqualTo(SESSION.getId());
			assertThat(tokens.getAccessToken()).isEqualTo(ACCESS_TOKEN);
			assertThat(tokens.getRefreshToken()).isEmpty();
			assertThat(tokens.getActiveUntil())
					.isEqualTo(SESSION.getActiveUntil());
		}

		@Test
		void sessionNull() {
			assertThatThrownBy(
					() -> new SessionTokens(null, ACCESS_TOKEN, REFRESH_TOKEN))
							.isInstanceOf(NullPointerException.class);
		}

		@Test
		void accessTokenNull() {
			final var sessionInfo = SESSION.info();
			assertThatThrownBy(
					() -> new SessionTokens(sessionInfo, null, REFRESH_TOKEN))
							.isInstanceOf(NullPointerException.class);
		}
	}
}
