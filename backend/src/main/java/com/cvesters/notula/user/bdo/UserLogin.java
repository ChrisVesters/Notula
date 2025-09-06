package com.cvesters.notula.user.bdo;

import java.util.Objects;

import org.apache.commons.lang3.Validate;

import lombok.Getter;

@Getter
public class UserLogin {
	// TODO: objects for email and password?
	private String email;
	private String password;

	public UserLogin(String email, String password) {
		Objects.requireNonNull(email);
		Validate.notBlank(email);
		// Validate.matchesPattern(email, password);
		Objects.requireNonNull(password);
		Validate.notBlank(password);
		// Validate.matchesPattern(email, password);

		this.email = email;
		this.password = password;
	}
}
