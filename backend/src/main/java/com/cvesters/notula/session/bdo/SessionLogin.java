package com.cvesters.notula.session.bdo;

import java.util.Objects;

import lombok.Getter;

import com.cvesters.notula.common.domain.Email;
import com.cvesters.notula.common.domain.Password;

// TODO: this is not a user login, it is a session login
// TODO: Actually the same object, isn't it?
@Getter
public class SessionLogin {
	private final Email email;
	private final Password password;

	public SessionLogin(final Email email, final Password password) {
		Objects.requireNonNull(email);
		Objects.requireNonNull(password);

		this.email = email;
		this.password = password;
	}
}
