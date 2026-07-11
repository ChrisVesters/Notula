package com.cvesters.notula.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.cvesters.notula.common.domain.Email;
import com.cvesters.notula.user.bdo.UserInfo;

public record UserCreateDto(
		@NotBlank @jakarta.validation.constraints.Email String email,
		@NotNull @Size(min = 8) String password) {

	public UserInfo toBdo() {
		final var validatedEmail = new Email(email);
		return new UserInfo(validatedEmail);
	}
}
