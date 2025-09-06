package com.cvesters.notula.user;

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
import org.springframework.test.context.jdbc.Sql;

import com.cvesters.notula.TestContainerConfig;
import com.cvesters.notula.user.dao.UserDao;

@Sql({ "/db/users.sql" })
@DataJpaTest
@Import(TestContainerConfig.class)
@AutoConfigureTestDatabase(replace = Replace.NONE)
class UserRepositoryTest {

	@Autowired
	private UserRepository userRepository;

	@PersistenceContext
	private EntityManager entityManager;

	@Nested
	class Save {

		private static final String EMAIL = "user@test";
		private static final String PASSWORD = "pass";

		@Test
		void success() {
			final var dao = new UserDao(EMAIL, PASSWORD);
			final UserDao saved = userRepository.save(dao);

			assertThat(saved.getId()).isNotNull();
			assertThat(saved.getEmail()).isEqualTo(EMAIL);
			assertThat(saved.getPassword()).isEqualTo(PASSWORD);

			final UserDao found = entityManager.find(UserDao.class, saved.getId());
			assertThat(found).isNotNull();
			assertThat(found.getId()).isEqualTo(saved.getId());
			assertThat(found.getEmail()).isEqualTo(EMAIL);
			assertThat(found.getPassword()).isEqualTo(PASSWORD);
		}

		@Test
		void emailNull() {
			final var dao = new UserDao(null, PASSWORD);
			assertThatThrownBy(() -> userRepository.save(dao))
					.isInstanceOf(DataIntegrityViolationException.class);
		}

		@Test
		void emailDuplicate() {
			final var email = TestUser.EDUARDO_CHRISTIANSEN.getEmail();
			final var dao = new UserDao(email, PASSWORD);

			assertThatThrownBy(() -> userRepository.save(dao))
					.isInstanceOf(DataIntegrityViolationException.class);
		}
	}
}
