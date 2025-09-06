package com.cvesters.notula.user;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;

import lombok.Getter;

import com.cvesters.notula.user.bdo.UserInfo;
import com.cvesters.notula.user.bdo.UserLogin;
import com.cvesters.notula.user.dao.UserDao;

@Getter
public enum TestUser {
	EDUARDO_CHRISTIANSEN(1L, "eduardo.christiansen@sporer.com",
			"bbkpHh_hKk6KMwv"),
	JUDY_HARBER(2L, "judy.harber@sporer.com", "wLITAlWOYY5J8ms");

	private final long id;
	private final String email;
	private final String password;

	private TestUser(final long id, final String email, final String password) {
		this.id = id;
		this.email = email;
		this.password = password;
	}

	public UserLogin login() {
		return new UserLogin(email, password);
	}

	public UserInfo info() {
		return new UserInfo(id, email);
	}

	// TODO: do we need this?
	public UserDao dao() throws Exception {
		final var dao = UserDao.class.getConstructor().newInstance();

		final Field idField = dao.getClass().getDeclaredField("id");
		idField.setAccessible(true);
		idField.set(dao, 1L);

		dao.setEmail(email);
		dao.setPassword(password);
		return dao;
	}

	public void assertEquals(final UserLogin login) {
		assertThat(login.getEmail()).isEqualTo(email);
		assertThat(login.getPassword()).isEqualTo(password);
	}

	public void assertEquals(final UserInfo info) {
		assertThat(info.getId()).isEqualTo(id);
		assertThat(info.getEmail()).isEqualTo(email);
	}
}
