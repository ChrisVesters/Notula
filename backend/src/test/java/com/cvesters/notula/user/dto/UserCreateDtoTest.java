package com.cvesters.notula.user.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.cvesters.notula.credential.TestCredential;
import com.cvesters.notula.user.TestUser;
import com.cvesters.notula.user.bdo.UserInfo;

class UserCreateDtoTest {

	private static final TestUser USER = TestUser.EDUARDO_CHRISTIANSEN;
	private static final TestCredential CREDENTIAL = TestCredential.EDUARDO_CHRISTIANSEN;

	@Test
	void toBdo() {
		final var dto = new UserCreateDto(USER.getEmail().value(),
				CREDENTIAL.getPassword().value());

		final UserInfo bdo = dto.toBdo();

		assertThat(bdo.getEmail()).isEqualTo(USER.getEmail());
		assertThat(bdo.getId()).isNull();
	}
}
