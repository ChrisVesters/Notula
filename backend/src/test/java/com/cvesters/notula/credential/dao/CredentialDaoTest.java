package com.cvesters.notula.credential.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.credential.TestCredential;
import com.cvesters.notula.credential.bdo.CredentialInfo;

class CredentialDaoTest {

	private static final TestCredential CREDENTIAL = TestCredential.EDUARDO_CHRISTIANSEN;
	private static final String PASSWORD = CREDENTIAL.getPassword().value();

	@Nested
	class Constructor {

		@Test
		void success() {
			final var dao = new CredentialDao(CREDENTIAL.getUserId(), PASSWORD);

			assertThat(dao.getId()).isNull();
			assertThat(dao.getUserId()).isEqualTo(CREDENTIAL.getUserId());
			assertThat(dao.getPassword()).isEqualTo(PASSWORD);
		}

		@Test
		void passwordNull() {
			final long userId = CREDENTIAL.getUserId();

			assertThatThrownBy(() -> new CredentialDao(userId, null))
					.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class ToBdo {

		private final CredentialDao dao = new CredentialDao(
				CREDENTIAL.getUserId(), PASSWORD);

		@Test
		void success() throws Exception {
			final Field idField = dao.getClass().getDeclaredField("id");
			idField.setAccessible(true);
			idField.set(dao, CREDENTIAL.getId());

			final CredentialInfo bdo = dao.toBdo();

			assertThat(bdo.getId()).isEqualTo(CREDENTIAL.getId());
			assertThat(bdo.getUserId()).isEqualTo(CREDENTIAL.getUserId());
		}

		@Test
		void idNull() {
			assertThatThrownBy(dao::toBdo)
					.isInstanceOf(IllegalStateException.class);
		}
	}

}
