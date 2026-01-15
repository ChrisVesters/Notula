package com.cvesters.notula.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Optional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.context.jdbc.Sql;

import com.cvesters.notula.TestContainerConfig;
import com.cvesters.notula.session.bdo.SessionCreate;
import com.cvesters.notula.session.dao.SessionDao;

@Sql({ "/db/users.sql", "/db/sessions.sql" })
@DataJpaTest
@Import(TestContainerConfig.class)
@AutoConfigureTestDatabase(replace = Replace.NONE)
class SessionRepositoryTest {

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_DEKSTOP;
	private static final String HASHED_REFRESH_TOKEN = "hash";

	@Autowired
	private SessionRepository sessionRepository;

	@PersistenceContext
	private EntityManager entityManager;

	@Nested
	class FindById {

		@Test
		void found() {
			final Optional<SessionDao> dao = sessionRepository
					.findById(SESSION.getId());

			final SessionDao expected = entityManager.find(SessionDao.class,
					SESSION.getId());
			assertThat(dao).contains(expected);
		}

		@Test
		void notFound() {
			final Optional<SessionDao> dao = sessionRepository
					.findById(Long.MAX_VALUE);

			assertThat(dao).isEmpty();
		}
	}

	@Nested
	class Save {

		@Test
		void success() {
			final var bdo = new SessionCreate(SESSION.getUser().info());
			final var dao = new SessionDao(bdo, HASHED_REFRESH_TOKEN);
			final SessionDao saved = sessionRepository.save(dao);

			assertThat(saved.getId()).isNotNull();
			assertThat(saved.getUserId()).isEqualTo(SESSION.getUser().getId());
			assertThat(saved.getRefreshToken())
					.isEqualTo(HASHED_REFRESH_TOKEN);
			assertThat(saved.getActiveUntil()).isEqualTo(bdo.getActiveUntil());

			final SessionDao found = entityManager.find(SessionDao.class,
					saved.getId());
			assertThat(found).isNotNull();
			assertThat(found.getId()).isEqualTo(saved.getId());
			assertThat(found.getUserId()).isEqualTo(SESSION.getUser().getId());
			assertThat(found.getRefreshToken())
					.isEqualTo(HASHED_REFRESH_TOKEN);
			assertThat(found.getActiveUntil()).isEqualTo(bdo.getActiveUntil());
		}

		@Test
		void sessionNull() {
			assertThatThrownBy(() -> sessionRepository.save(null))
					.isInstanceOf(InvalidDataAccessApiUsageException.class);
		}
	}
}
