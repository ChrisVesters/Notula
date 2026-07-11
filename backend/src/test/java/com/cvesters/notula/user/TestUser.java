package com.cvesters.notula.user;

import lombok.Getter;

import com.cvesters.notula.common.domain.Email;
import com.cvesters.notula.user.bdo.UserInfo;

@Getter
public enum TestUser {
	EDUARDO_CHRISTIANSEN(1L, "eduardo.christiansen@sporer.com"),
	KRISTINA_THIEL(2L, "kristina.thiel@sporer.com"),
	DAPHNEE_LESCH(3L, "daphnee.lesch@sporer.com"),
	ALISON_DACH(4L, "alison_dach@glover-group.co.uk");

	private final long id;
	private final Email email;

	TestUser(final long id, final String email) {
		this.id = id;
		this.email = new Email(email);
	}

	public UserInfo info() {
		return new UserInfo(id, email);
	}
}
