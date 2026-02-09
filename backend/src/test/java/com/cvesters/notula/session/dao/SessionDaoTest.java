package com.cvesters.notula.session.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.session.TestSession;
import com.cvesters.notula.session.bdo.SessionInfo;

class SessionDaoTest {

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
	private static final String HASHED_REFRESH_TOKEN = "hash";

	@Nested
	class Constructor {

		@Test
		void success() {
			final long userId = SESSION.getUser().getId();
			final long orgId = SESSION.getOrganisation().getId();
			final var bdo = new SessionInfo(userId, orgId);

			final var dao = new SessionDao(bdo, HASHED_REFRESH_TOKEN);

			assertThat(dao.getId()).isNull();
			assertThat(dao.getUserId()).isEqualTo(bdo.getUserId());
			assertThat(dao.getOrganisationId()).isNull();
			assertThat(dao.getRefreshToken()).isEqualTo(HASHED_REFRESH_TOKEN);
			assertThat(dao.getActiveUntil()).isEqualTo(bdo.getActiveUntil());
		}

		@Test
		void bdoNull() {
			assertThatThrownBy(() -> new SessionDao(null, HASHED_REFRESH_TOKEN))
					.isInstanceOf(NullPointerException.class);
		}

		@Test
		void refreshTokenNull() {
			final long userId = SESSION.getUser().getId();
			final long orgId = SESSION.getOrganisation().getId();
			final var bdo = new SessionInfo(userId, orgId);

			assertThatThrownBy(() -> new SessionDao(bdo, null))
					.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class Update {

		private final static SessionInfo BDO = SESSION.info();
		private SessionDao dao;

		@BeforeEach
		void setup() throws Exception {
			dao = new SessionDao(BDO, HASHED_REFRESH_TOKEN);
			final var idField = dao.getClass().getDeclaredField("id");
			idField.setAccessible(true);
			idField.set(dao, SESSION.getId());
		}

		@Test
		void success() {
			final long organisationId = 22;
			final var activeUntil = OffsetDateTime.now().plusDays(5);

			final SessionInfo update = mock();
			when(update.getId()).thenReturn(SESSION.getId());
			when(update.getOrganisationId())
					.thenReturn(Optional.of(organisationId));
			when(update.getActiveUntil()).thenReturn(activeUntil);

			dao.update(update);

			assertThat(dao.getId()).isEqualTo(SESSION.getId());
			assertThat(dao.getUserId()).isEqualTo(SESSION.getUser().getId());
			assertThat(dao.getOrganisationId()).isEqualTo(organisationId);
			assertThat(dao.getRefreshToken()).isEqualTo(HASHED_REFRESH_TOKEN);
			assertThat(dao.getActiveUntil()).isEqualTo(activeUntil);
		}

		@Test
		void wrongSessionId() {
			final SessionInfo update = mock();
			when(update.getId()).thenReturn(Long.MAX_VALUE);

			assertThatThrownBy(() -> dao.update(update))
					.isInstanceOf(IllegalArgumentException.class);
		}

		@Test
		void updateNull() {
			assertThatThrownBy(() -> dao.update(null))
					.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class UpdateWithRefreshToken {

		private static final String UPDATED_REFRESH_TOKEN = "newHash";
		private static final SessionInfo BDO = SESSION.info();
		private SessionDao dao;

		@BeforeEach
		void setup() throws Exception {
			dao = new SessionDao(BDO, HASHED_REFRESH_TOKEN);
			final var idField = dao.getClass().getDeclaredField("id");
			idField.setAccessible(true);
			idField.set(dao, SESSION.getId());
		}

		@Test
		void success() {
			final long organisationId = 22;
			final var activeUntil = OffsetDateTime.now().plusDays(5);

			final SessionInfo update = mock();
			when(update.getId()).thenReturn(SESSION.getId());
			when(update.getOrganisationId())
					.thenReturn(Optional.of(organisationId));
			when(update.getActiveUntil()).thenReturn(activeUntil);

			dao.update(update, UPDATED_REFRESH_TOKEN);

			assertThat(dao.getId()).isEqualTo(SESSION.getId());
			assertThat(dao.getUserId()).isEqualTo(SESSION.getUser().getId());
			assertThat(dao.getOrganisationId()).isEqualTo(organisationId);
			assertThat(dao.getRefreshToken()).isEqualTo(UPDATED_REFRESH_TOKEN);
			assertThat(dao.getActiveUntil()).isEqualTo(activeUntil);
		}

		@Test
		void wrongSessionId() {
			final SessionInfo update = mock();
			when(update.getId()).thenReturn(Long.MAX_VALUE);

			assertThatThrownBy(() -> dao.update(update, UPDATED_REFRESH_TOKEN))
					.isInstanceOf(IllegalArgumentException.class);
		}

		@Test
		void updateNull() {
			assertThatThrownBy(() -> dao.update(null, UPDATED_REFRESH_TOKEN))
					.isInstanceOf(NullPointerException.class);
		}

		@Test
		void refreshTokenNull() {
			final SessionInfo update = mock();

			assertThatThrownBy(() -> dao.update(update, null))
					.isInstanceOf(NullPointerException.class);

			verifyNoInteractions(update);
		}

	}

	@Nested
	class ToBdo {

		private final SessionInfo bdo = mock();
		private SessionDao dao;

		@BeforeEach
		void setup() {
			when(bdo.getUserId()).thenReturn(SESSION.getUser().getId());
			when(bdo.getActiveUntil()).thenReturn(SESSION.getActiveUntil());

			dao = new SessionDao(bdo, HASHED_REFRESH_TOKEN);
		}

		@Test
		void withoutOrganisation() throws Exception {
			final var idField = dao.getClass().getDeclaredField("id");
			idField.setAccessible(true);
			idField.set(dao, SESSION.getId());

			final SessionInfo bdo = dao.toBdo();

			assertThat(bdo.getId()).isEqualTo(SESSION.getId());
			assertThat(bdo.getUserId()).isEqualTo(SESSION.getUser().getId());
			assertThat(bdo.getOrganisationId()).isEmpty();
			assertThat(bdo.getActiveUntil())
					.isEqualTo(SESSION.getActiveUntil());
		}

		@Test
		void withOrganisation() throws Exception {
			final var idField = dao.getClass().getDeclaredField("id");
			idField.setAccessible(true);
			idField.set(dao, SESSION.getId());

			final var organisationIdField = dao.getClass()
					.getDeclaredField("organisationId");
			organisationIdField.setAccessible(true);
			organisationIdField.set(dao, SESSION.getOrganisation().getId());

			final SessionInfo bdo = dao.toBdo();

			assertThat(bdo.getId()).isEqualTo(SESSION.getId());
			assertThat(bdo.getUserId()).isEqualTo(SESSION.getUser().getId());
			assertThat(bdo.getOrganisationId())
					.contains(SESSION.getOrganisation().getId());
			assertThat(bdo.getActiveUntil())
					.isEqualTo(SESSION.getActiveUntil());
		}

		@Test
		void idNull() {
			assertThatThrownBy(dao::toBdo)
					.isInstanceOf(IllegalStateException.class);
		}
	}
}
