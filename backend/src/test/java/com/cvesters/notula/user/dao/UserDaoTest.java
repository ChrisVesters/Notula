package com.cvesters.notula.user.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.user.TestUser;
import com.cvesters.notula.user.bdo.UserInfo;

class UserDaoTest {

	private static final TestUser USER = TestUser.EDUARDO_CHRISTIANSEN;
	private final UserDao dao = new UserDao(USER.getEmail().value(),
			USER.getPassword().value());

	@Nested
	class ToBdo {

		@Test
		void withoutId() {
			final UserInfo info = dao.toBdo();

			assertThat(info.getId()).isNull();
			assertThat(info.getEmail()).isEqualTo(USER.getEmail());
		}

		@Test
		void withId() throws Exception {
			final Field idField = dao.getClass().getDeclaredField("id");
			idField.setAccessible(true);
			idField.set(dao, USER.getId());

			final var bdo = dao.toBdo();

			assertThat(bdo.getId()).isEqualTo(USER.getId());
			assertThat(bdo.getEmail()).isEqualTo(USER.getEmail());
		}
	}
}
