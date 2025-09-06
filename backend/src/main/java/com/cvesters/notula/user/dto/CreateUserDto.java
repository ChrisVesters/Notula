package com.cvesters.notula.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import com.cvesters.notula.user.bdo.UserLogin;

public record CreateUserDto(@Email String email,
		@Size(min = 8) String password) {

	public UserLogin toBdo() {
		return new UserLogin(email, password);
	}
}
