package com.cvesters.notula.user.bdo;

import java.util.Objects;

import lombok.Getter;

import com.cvesters.notula.common.domain.Email;
import com.cvesters.notula.common.domain.Password;

@Getter
public class UserLogin {

	private final Email email;
	private final Password password;

	public UserLogin(final Email email, final Password password) {
		Objects.requireNonNull(email);
		Objects.requireNonNull(password);

		this.email = email;
		this.password = password;
	}
}
