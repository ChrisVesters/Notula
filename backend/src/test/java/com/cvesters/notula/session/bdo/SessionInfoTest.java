package com.cvesters.notula.session.bdo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.session.TestSession;

class SessionInfoTest {

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_DEKSTOP;

	@Nested
	class Constructor {

		@Test
		void withId() {
			final var sessionInfo = new SessionInfo(SESSION.getId(),
					SESSION.getUser().getId(),
					SESSION.getActiveUntil());

			assertThat(sessionInfo.getId()).isEqualTo(SESSION.getId());
			assertThat(sessionInfo.getUserId())
					.isEqualTo(SESSION.getUser().getId());
			assertThat(sessionInfo.getActiveUntil())
					.isEqualTo(SESSION.getActiveUntil());
		}

		@Test
		void activeUntilNull() {
			final long id = SESSION.getId();
			final long userId = SESSION.getUser().getId();
			final OffsetDateTime activeUntil = null;

			assertThatThrownBy(() -> new SessionInfo(id, userId,
					activeUntil)).isInstanceOf(NullPointerException.class);
		}
	}
}
