package com.cvesters.notula.session.bdo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import java.time.Duration;
import java.time.OffsetDateTime;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.session.TestSession;

class SessionCreateTest {

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;

	@Nested
	class Constructor {

		@Test
		void success() {
			final var action = new SessionCreate(
					SESSION.getUser().info());

			assertThat(action.getUserId()).isEqualTo(SESSION.getUser().getId());
			assertThat(action.getRefreshToken()).matches("[\\p{Alnum}-_]{86}");
			assertThat(action.getActiveUntil()).isCloseTo(
					OffsetDateTime.now().plus(Duration.ofDays(7)),
					within(Duration.ofSeconds(1)));
		}

		@Test
		void userNull() {
			assertThatThrownBy(() -> new SessionCreate(null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
