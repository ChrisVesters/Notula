package com.cvesters.notula.user.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.cvesters.notula.user.TestUser;
import com.cvesters.notula.user.bdo.UserLogin;

class UserCreateDtoTest {

	private static final TestUser USER = TestUser.EDUARDO_CHRISTIANSEN;

	@Test
	void toBdo() {
		final var dto = new UserCreateDto(USER.getEmail().value(),
				USER.getPassword().value());

		final UserLogin bdo = dto.toBdo();

		assertThat(bdo.getEmail()).isEqualTo(USER.getEmail());
		assertThat(bdo.getPassword()).isEqualTo(USER.getPassword());
	}
}
