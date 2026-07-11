package com.cvesters.notula.credential;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.context.jdbc.Sql;

import com.cvesters.notula.credential.dao.CredentialDao;
import com.cvesters.notula.test.RepositoryTest;

@Sql({ "/db/users.sql", "/db/credentials.sql" })
class CredentialRepositoryTest extends RepositoryTest {

	private static final TestCredential CREDENTIAL = TestCredential.EDUARDO_CHRISTIANSEN;

	@Autowired
	private CredentialRepository credentialRepository;

	@Nested
	class ExistsByUserId {

		@Test
		void exists() {
			final boolean exists = credentialRepository
					.existsByUserId(CREDENTIAL.getUserId());

			assertThat(exists).isTrue();
		}

		@Test
		void notExists() {
			final boolean exists = credentialRepository
					.existsByUserId(Long.MAX_VALUE);

			assertThat(exists).isFalse();
		}
	}

	@Nested
	class FindByUserId {

		@Test
		void found() {
			final Optional<CredentialDao> dao = credentialRepository
					.findByUserId(CREDENTIAL.getUserId());

			assertThat(dao).get().satisfies(found -> {
				assertThat(found.getId()).isEqualTo(CREDENTIAL.getId());
				assertThat(found.getUserId()).isEqualTo(CREDENTIAL.getUserId());
				assertThat(found.getPassword())
						.isEqualTo(CREDENTIAL.getPassword().value());
			});

			final CredentialDao expected = entityManager
					.find(CredentialDao.class, CREDENTIAL.getId());
			assertThat(dao).contains(expected);

		}

		@Test
		void notFound() {
			final Optional<CredentialDao> dao = credentialRepository
					.findByUserId(Long.MAX_VALUE);

			assertThat(dao).isEmpty();
		}
	}

	@Nested
	class Save {

		@Test
		void success() {
			final long userId = 5L;
			final String password = "password";

			final var dao = new CredentialDao(userId, password);
			final CredentialDao saved = credentialRepository.save(dao);

			assertThat(saved.getId()).isNotNull();
			assertThat(saved.getUserId()).isEqualTo(userId);
			assertThat(saved.getPassword()).isEqualTo(password);

			final CredentialDao found = entityManager.find(CredentialDao.class,
					saved.getId());
			assertThat(found).isNotNull();
			assertThat(found.getId()).isEqualTo(saved.getId());
			assertThat(found.getUserId()).isEqualTo(userId);
			assertThat(found.getPassword()).isEqualTo(password);
		}

		@Test
		void userMissing() {
			final var dao = new CredentialDao(Long.MAX_VALUE, "password");

			assertThatThrownBy(() -> credentialRepository.save(dao))
					.isInstanceOf(DataIntegrityViolationException.class);
		}

		@Test
		void credentialNull() {
			assertThatThrownBy(() -> credentialRepository.save(null))
					.isInstanceOf(InvalidDataAccessApiUsageException.class);
		}
	}
}
