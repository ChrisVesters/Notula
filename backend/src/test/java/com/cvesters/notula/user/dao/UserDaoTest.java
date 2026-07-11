package com.cvesters.notula.user.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.user.TestUser;

class UserDaoTest {

	private static final TestUser USER = TestUser.EDUARDO_CHRISTIANSEN;

	@Nested
	class Constructor {

		@Test
		void success() {
			final var dao = new UserDao(USER.getEmail().value());

			assertThat(dao.getId()).isNull();
			assertThat(dao.getEmail()).isEqualTo(USER.getEmail().value());
		}

		@Test
		void emailNull() {
			assertThatThrownBy(() -> new UserDao(null))
					.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class ToBdo {

		private final UserDao dao = new UserDao(USER.getEmail().value());

		@Test
		void success() throws Exception {
			final Field idField = dao.getClass().getDeclaredField("id");
			idField.setAccessible(true);
			idField.set(dao, USER.getId());

			final var bdo = dao.toBdo();

			assertThat(bdo.getId()).isEqualTo(USER.getId());
			assertThat(bdo.getEmail()).isEqualTo(USER.getEmail());
		}

		@Test
		void idNull() {
			assertThatThrownBy(dao::toBdo)
					.isInstanceOf(IllegalStateException.class);
		}
	}

}
