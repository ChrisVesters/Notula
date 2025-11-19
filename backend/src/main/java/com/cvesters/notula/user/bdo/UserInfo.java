package com.cvesters.notula.user.bdo;

import java.util.Objects;

import lombok.Getter;

import com.cvesters.notula.common.domain.Email;

@Getter
public class UserInfo {

	private final long id;
	private Email email;

	public UserInfo(final long id, final Email email) {
		Objects.requireNonNull(email);

		this.id = id;
		this.email = email;
	}
}
