package com.cvesters.notula.user.bdo;

import java.util.Objects;

import lombok.Getter;

import com.cvesters.notula.common.domain.Email;

@Getter
public class UserInfo {

	private Long id;
	private Email email;

	public UserInfo(final Long id, final Email email) {
		Objects.requireNonNull(email);

		this.id = id;
		this.email = email;
	}
}
