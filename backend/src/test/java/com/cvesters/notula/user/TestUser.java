package com.cvesters.notula.user;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.Getter;

import com.cvesters.notula.common.domain.Email;
import com.cvesters.notula.common.domain.Password;
import com.cvesters.notula.user.bdo.UserInfo;
import com.cvesters.notula.user.bdo.UserLogin;

@Getter
public enum TestUser {
	EDUARDO_CHRISTIANSEN(1L, "eduardo.christiansen@sporer.com",
			"bbkpHh_hKk6KMwv"),
	JUDY_HARBER(2L, "judy.harber@sporer.com", "wLITAlWOYY5J8ms");

	private final long id;
	private final Email email;
	private final Password password;

	TestUser(final long id, final String email, final String password) {
		this.id = id;
		this.email = new Email(email);
		this.password = new Password(password);
	}

	public UserLogin login() {
		return new UserLogin(email, password);
	}

	public UserInfo info() {
		return new UserInfo(id, email);
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
