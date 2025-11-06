package com.cvesters.notula.user.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

import com.cvesters.notula.user.TestUser;

class UserDaoTest {

	private static final TestUser USER = TestUser.EDUARDO_CHRISTIANSEN;
	private final UserDao dao = new UserDao(USER.getEmail().value(),
			USER.getPassword().value());

	@Test
	void toBdo() throws Exception {
		final Field idField = dao.getClass().getDeclaredField("id");
		idField.setAccessible(true);
		idField.set(dao, USER.getId());

		final var bdo = dao.toBdo();

		assertThat(bdo.getId()).isEqualTo(USER.getId());
		assertThat(bdo.getEmail()).isEqualTo(USER.getEmail());
	}
}
