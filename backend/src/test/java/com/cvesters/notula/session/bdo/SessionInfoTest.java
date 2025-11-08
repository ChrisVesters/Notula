package com.cvesters.notula.session.bdo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import java.time.Duration;
import java.time.OffsetDateTime;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.session.TestSession;

class SessionInfoTest {

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_DEKSTOP;

	@Nested
	class Constructor {

		@Test
		void minimal() {
			final var sessionInfo = new SessionInfo(SESSION.getUser().getId());

			assertThat(sessionInfo.getId()).isNull();
			assertThat(sessionInfo.getUserId())
					.isEqualTo(SESSION.getUser().getId());
			assertThat(sessionInfo.getRefreshToken())
					.isEqualTo(SESSION.getRefreshToken());
					// TODO: matches a certain pattern.
			assertThat(sessionInfo.getActiveUntil())
					.isCloseTo(OffsetDateTime.now(),
							within(Duration.ofSeconds(1)));
		}

		@Test
		void withId() {
			final var sessionInfo = new SessionInfo(SESSION.getId(),
					SESSION.getUser().getId(), SESSION.getRefreshToken(),
					SESSION.getActiveUntil());

			assertThat(sessionInfo.getId()).isEqualTo(SESSION.getId());
			assertThat(sessionInfo.getUserId())
					.isEqualTo(SESSION.getUser().getId());
			assertThat(sessionInfo.getRefreshToken())
					.isEqualTo(SESSION.getRefreshToken());
			assertThat(sessionInfo.getActiveUntil())
					.isEqualTo(SESSION.getActiveUntil());
		}

		@Test
		void idNull() {
			final var sessionInfo = new SessionInfo(null,
					SESSION.getUser().getId(), SESSION.getRefreshToken(),
					SESSION.getActiveUntil());

			assertThat(sessionInfo.getId()).isNull();
			assertThat(sessionInfo.getUserId())
					.isEqualTo(SESSION.getUser().getId());
			assertThat(sessionInfo.getRefreshToken())
					.isEqualTo(SESSION.getRefreshToken());
			assertThat(sessionInfo.getActiveUntil())
					.isEqualTo(SESSION.getActiveUntil());
		}

		@Test
		void refreshTokenNull() {
			final long id = SESSION.getId();
			final long userId = SESSION.getUser().getId();
			final String refreshToken = null;
			final OffsetDateTime activeUntil = SESSION.getActiveUntil();

			assertThatThrownBy(() -> new SessionInfo(id, userId, refreshToken,
					activeUntil)).isInstanceOf(NullPointerException.class);
		}

		@Test
		void activeUntilNull() {
			final long id = SESSION.getId();
			final long userId = SESSION.getUser().getId();
			final String refreshToken = SESSION.getRefreshToken();
			final OffsetDateTime activeUntil = null;

			assertThatThrownBy(() -> new SessionInfo(id, userId, refreshToken,
					activeUntil)).isInstanceOf(NullPointerException.class);
		}
	}
}
