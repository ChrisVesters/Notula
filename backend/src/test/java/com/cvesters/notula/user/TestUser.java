package com.cvesters.notula.user;

import lombok.Getter;

import com.cvesters.notula.common.domain.Email;
import com.cvesters.notula.common.domain.Password;
import com.cvesters.notula.user.bdo.UserInfo;
import com.cvesters.notula.user.bdo.UserLogin;

@Getter
public enum TestUser {
	EDUARDO_CHRISTIANSEN(1L, "eduardo.christiansen@sporer.com",
			"bbkpHh_hKk6KMwv"),
	KRISTINA_THIEL(2L, "kristina.thiel@sporer.com", "wLITAlWOYY5J8ms"),
	DAPHNEE_LESCH(3L, "daphnee.lesch@sporer.com", "VIz3jmembRtsuoo"),
	ALISON_DACH(4L, "alison_dach@glover-group.co.uk", "YIHS3bbkpHh_hKk");

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
}
