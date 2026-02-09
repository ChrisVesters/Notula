package com.cvesters.notula.session.bdo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.Period;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.session.TestSession;

class SessionInfoTest {

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;

	@Nested
	class Constructor {

		@Test
		void withoutId() {
			final var sessionInfo = new SessionInfo(SESSION.getUser().getId(),
					SESSION.getOrganisation().getId());

			assertThatThrownBy(() -> sessionInfo.getId())
					.isInstanceOf(IllegalStateException.class);
			assertThat(sessionInfo.getUserId())
					.isEqualTo(SESSION.getUser().getId());
			assertThat(sessionInfo.getOrganisationId())
					.contains(SESSION.getOrganisation().getId());
			assertThat(sessionInfo.getActiveUntil()).isCloseTo(
					OffsetDateTime.now().plus(Period.ofDays(7)),
					within(Duration.ofMillis(100)));
		}

		@Test
		void withIdWithoutOrganisationId() {
			final var sessionInfo = new SessionInfo(SESSION.getId(),
					SESSION.getUser().getId(), null, SESSION.getActiveUntil());

			assertThat(sessionInfo.getId()).isEqualTo(SESSION.getId());
			assertThat(sessionInfo.getUserId())
					.isEqualTo(SESSION.getUser().getId());
			assertThat(sessionInfo.getOrganisationId()).isEmpty();
			assertThat(sessionInfo.getActiveUntil())
					.isEqualTo(SESSION.getActiveUntil());
		}

		@Test
		void full() {
			final var sessionInfo = new SessionInfo(SESSION.getId(),
					SESSION.getUser().getId(),
					SESSION.getOrganisation().getId(),
					SESSION.getActiveUntil());

			assertThat(sessionInfo.getId()).isEqualTo(SESSION.getId());
			assertThat(sessionInfo.getUserId())
					.isEqualTo(SESSION.getUser().getId());
			assertThat(sessionInfo.getOrganisationId())
					.contains(SESSION.getOrganisation().getId());
			assertThat(sessionInfo.getActiveUntil())
					.isEqualTo(SESSION.getActiveUntil());
		}

		@Test
		void activeUntilNull() {
			final long id = SESSION.getId();
			final long userId = SESSION.getUser().getId();
			final long organisationId = SESSION.getOrganisation().getId();
			final OffsetDateTime activeUntil = null;

			assertThatThrownBy(() -> new SessionInfo(id, userId, organisationId,
					activeUntil)).isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class Update {

		private final SessionInfo sessionInfo = SESSION.info();

		@Test
		void success() {
			final long organisationId = SESSION.getOrganisation().getId() + 1;
			final var update = new SessionUpdate(organisationId);

			sessionInfo.update(update);

			assertThat(sessionInfo.getId()).isEqualTo(SESSION.getId());
			assertThat(sessionInfo.getUserId())
					.isEqualTo(SESSION.getUser().getId());
			assertThat(sessionInfo.getOrganisationId())
					.contains(organisationId);
			assertThat(sessionInfo.getActiveUntil())
					.isEqualTo(SESSION.getActiveUntil());
		}

		@Test
		void updateNull() {
			assertThatThrownBy(() -> sessionInfo.update(null))
					.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class Refresh {

		@Test
		void success() {
			final var sessionInfo = new SessionInfo(SESSION.getId(),
					SESSION.getUser().getId(),
					SESSION.getOrganisation().getId(),
					OffsetDateTime.now().plus(Duration.ofHours(4)));

			sessionInfo.refresh();

			assertThat(sessionInfo.getId()).isEqualTo(SESSION.getId());
			assertThat(sessionInfo.getUserId())
					.isEqualTo(SESSION.getUser().getId());
			assertThat(sessionInfo.getOrganisationId())
					.contains(SESSION.getOrganisation().getId());
			assertThat(sessionInfo.getActiveUntil()).isCloseTo(
					OffsetDateTime.now().plus(Period.ofDays(7)),
					within(Duration.ofSeconds(1)));
		}

		@Test
		void expiredSession() {
			final var sessionInfo = new SessionInfo(SESSION.getId(),
					SESSION.getUser().getId(),
					SESSION.getOrganisation().getId(),
					OffsetDateTime.now().minus(Duration.ofHours(4)));

			assertThatThrownBy(() -> sessionInfo.refresh())
					.isInstanceOf(IllegalStateException.class);
		}
	}
}
