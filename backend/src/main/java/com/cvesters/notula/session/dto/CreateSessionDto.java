package com.cvesters.notula.session.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.cvesters.notula.common.domain.Email;
import com.cvesters.notula.common.domain.Password;
import com.cvesters.notula.session.bdo.SessionLogin;

public record CreateSessionDto (@NotBlank @jakarta.validation.constraints.Email String email,
		@NotNull @Size(min = 8) String password) {

			// TODO: test!!!
	public SessionLogin toBdo() {
		final var validatedEmail = new Email(email);
		final var validatedPassword = new Password(password);
		return new SessionLogin(validatedEmail, validatedPassword);
	}
}