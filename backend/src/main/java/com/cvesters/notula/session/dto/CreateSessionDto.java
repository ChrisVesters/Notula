package com.cvesters.notula.session.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateSessionDto (@NotBlank @jakarta.validation.constraints.Email String email,
		@NotNull @Size(min = 8) String password) {

	// public Session toBdo() {
	// 	final var validatedEmail = new Email(email);
	// 	final var validatedPassword = new Password(password);
	// 	return new Session(validatedEmail, validatedPassword);
	// }
}