package com.cvesters.notula.session.bdo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.session.TestSession;

public class SessionTokensTest {

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_DEKSTOP;
	private static final String ACCESS_TOKEN = "access_token";

	@Nested
	class Constructor {

		@Test
		void success() {
			final var tokens = new SessionTokens(SESSION.info(), ACCESS_TOKEN);

			assertThat(tokens.getId()).isEqualTo(SESSION.getId());
			assertThat(tokens.getAccessToken()).isEqualTo(ACCESS_TOKEN);
			assertThat(tokens.getRefreshToken())
					.isEqualTo(SESSION.getRefreshToken());
			assertThat(tokens.getActiveUntil())
					.isEqualTo(SESSION.getActiveUntil());
		}

		@Test
		void sessionNull() {
			assertThatThrownBy(() -> new SessionTokens(null, ACCESS_TOKEN))
					.isInstanceOf(NullPointerException.class);
		}

		@Test
		void sessionIdNull() {
			final var sessionInfo = new SessionInfo(null,
					SESSION.getUser().getId(), SESSION.getRefreshToken(),
					SESSION.getActiveUntil());

			assertThatThrownBy(
					() -> new SessionTokens(sessionInfo, ACCESS_TOKEN))
							.isInstanceOf(NullPointerException.class);
		}

		@Test
		void accessTokenNull() {
			final var sessionInfo = SESSION.info();
			assertThatThrownBy(() -> new SessionTokens(sessionInfo, null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
