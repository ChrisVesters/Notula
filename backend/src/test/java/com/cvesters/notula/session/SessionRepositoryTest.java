package com.cvesters.notula.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.context.jdbc.Sql;

import com.cvesters.notula.TestContainerConfig;
import com.cvesters.notula.session.dao.SessionDao;

@Sql({ "/db/users.sql", "/db/sessions.sql" })
@DataJpaTest
@Import(TestContainerConfig.class)
@AutoConfigureTestDatabase(replace = Replace.NONE)
class SessionRepositoryTest {

	@Autowired
	private SessionRepository sessionRepository;

	@PersistenceContext
	private EntityManager entityManager;

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_DEKSTOP;

	@Nested
	class Save {

		private static final String REFRESH_TOKEN = "aaaa";

		@Test
		void success() {
			final var dao = new SessionDao(SESSION.getUser().getId(),
					REFRESH_TOKEN, SESSION.getActiveUntil());
			final SessionDao saved = sessionRepository.save(dao);

			assertThat(saved.getId()).isNotNull();
			assertThat(saved.getUserId()).isEqualTo(SESSION.getUser().getId());
			assertThat(saved.getRefreshToken()).isEqualTo(REFRESH_TOKEN);
			assertThat(saved.getActiveUntil())
					.isEqualTo(SESSION.getActiveUntil());

			final SessionDao found = entityManager.find(SessionDao.class,
					saved.getId());
			assertThat(found).isNotNull();
			assertThat(found.getId()).isEqualTo(saved.getId());
			assertThat(found.getUserId()).isEqualTo(SESSION.getUser().getId());
			assertThat(found.getRefreshToken()).isEqualTo(REFRESH_TOKEN);
			assertThat(found.getActiveUntil())
					.isEqualTo(SESSION.getActiveUntil());
		}

		@Test
		void refreshTokenNull() {
			final var dao = new SessionDao(SESSION.getUser().getId(), null,
					SESSION.getActiveUntil());

			assertThatThrownBy(() -> sessionRepository.save(dao))
					.isInstanceOf(DataIntegrityViolationException.class);
		}

		@Test
		void refreshTokenDuplicate() {
			final var dao = new SessionDao(SESSION.getUser().getId(),
					SESSION.getRefreshToken(), SESSION.getActiveUntil());

			assertThatThrownBy(() -> sessionRepository.save(dao))
					.isInstanceOf(DataIntegrityViolationException.class);
		}

		@Test
		void activeUntilNull() {
			final var dao = new SessionDao(SESSION.getUser().getId(),
					REFRESH_TOKEN, null);

			assertThatThrownBy(() -> sessionRepository.save(dao))
					.isInstanceOf(DataIntegrityViolationException.class);
		}

		@Test
		void sessionNull() {
			assertThatThrownBy(() -> sessionRepository.save(null))
					.isInstanceOf(InvalidDataAccessApiUsageException.class);
		}
	}
}
