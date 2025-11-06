package com.cvesters.notula.session.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.cvesters.notula.session.TestSession;
import com.cvesters.notula.user.TestUser;
import com.cvesters.notula.user.bdo.UserLogin;

class CreateSessionDtoTest {

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_DEKSTOP;
	private static final TestUser USER = SESSION.getUser();

	@Test
	void toBdo() {
		final var dto = new CreateSessionDto(USER.getEmail().value(),
				USER.getPassword().value());

		final UserLogin bdo = dto.toBdo();

		assertThat(bdo.getEmail()).isEqualTo(USER.getEmail());
		assertThat(bdo.getPassword()).isEqualTo(USER.getPassword());
	}
}
