package com.cvesters.notula.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record CreateUserDto(@Email String email,
		@Size(min = 8) String password) {

}
