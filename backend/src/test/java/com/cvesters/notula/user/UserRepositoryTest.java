package com.cvesters.notula.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.context.jdbc.Sql;

import com.cvesters.notula.TestContainerConfig;
import com.cvesters.notula.user.dao.UserDao;

@Sql({ "/db/users.sql" })
@DataJpaTest
@Import(TestContainerConfig.class)
@AutoConfigureTestDatabase(replace = Replace.NONE)
class UserRepositoryTest {

	private static final TestUser USER = TestUser.EDUARDO_CHRISTIANSEN;

	@Autowired
	private UserRepository userRepository;

	@PersistenceContext
	private EntityManager entityManager;

	@Nested
	class ExistsByEmail {

		@Test
		void found() {
			assertThat(userRepository.existsByEmail(USER.getEmail().value()))
					.isTrue();
		}

		@Test
		void notFound() {
			assertThat(userRepository.existsByEmail("user@test")).isFalse();
		}

		@Test
		void emailNull() {
			assertThat(userRepository.existsByEmail(null)).isFalse();
		}

		@ParameterizedTest
		@ValueSource(strings = { "x'; DROP TABLE users; --", "' OR '1'='1", "'; SELECT * FROM users WHERE 'a'='a" })
		void sqlInjection(final String sqlInjection) {
			assertThat(userRepository.existsByEmail(sqlInjection)).isFalse();
		}
	}

	@Nested
	class Save {

		@Test
		void success() {
			final String email = "user@test";
			final String password = "pass";

			final var dao = new UserDao(email, password);
			final UserDao saved = userRepository.save(dao);

			assertThat(saved.getId()).isNotNull();
			assertThat(saved.getEmail()).isEqualTo(email);
			assertThat(saved.getPassword()).isEqualTo(password);

			final UserDao found = entityManager.find(UserDao.class,
					saved.getId());
			assertThat(found).isNotNull();
			assertThat(found.getId()).isEqualTo(saved.getId());
			assertThat(found.getEmail()).isEqualTo(email);
			assertThat(found.getPassword()).isEqualTo(password);
		}

		@Test
		void emailNull() {
			final var dao = new UserDao(null, USER.getPassword().value());
			assertThatThrownBy(() -> userRepository.save(dao))
					.isInstanceOf(DataIntegrityViolationException.class);
		}

		@Test
		void emailDuplicate() {
			final var dao = new UserDao(USER.getEmail().value(),
					USER.getPassword().value());

			assertThatThrownBy(() -> userRepository.save(dao))
					.isInstanceOf(DataIntegrityViolationException.class);
		}

		@Test
		void userNull() {
			assertThatThrownBy(() -> userRepository.save(null))
					.isInstanceOf(InvalidDataAccessApiUsageException.class);
		}
	}
}
