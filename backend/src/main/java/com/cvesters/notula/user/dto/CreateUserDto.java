package com.cvesters.notula.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.cvesters.notula.common.domain.Email;
import com.cvesters.notula.common.domain.Password;
import com.cvesters.notula.user.bdo.UserLogin;

public record CreateUserDto(@NotBlank @jakarta.validation.constraints.Email String email,
		@NotBlank @Size(min = 8) String password) {

	public UserLogin toBdo() {
		final var validatedEmail = new Email(email);
		final var validatedPassword = new Password(password);
		return new UserLogin(validatedEmail, validatedPassword);
	}
}
