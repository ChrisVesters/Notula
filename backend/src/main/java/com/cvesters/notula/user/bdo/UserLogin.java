package com.cvesters.notula.user.bdo;

import java.util.Objects;

import lombok.Getter;

import com.cvesters.notula.common.domain.Email;
import com.cvesters.notula.common.domain.Password;

@Getter
public class UserLogin {
	// TODO: objects for email and password?
	private final Email email;
	private final Password password;

	public UserLogin(final Email email, final Password password) {
		Objects.requireNonNull(email);
		// Validate.matchesPattern(email, password);
		Objects.requireNonNull(password);
		// Validate.matchesPattern(email, password);

		this.email = email;
		this.password = password;
	}
}
