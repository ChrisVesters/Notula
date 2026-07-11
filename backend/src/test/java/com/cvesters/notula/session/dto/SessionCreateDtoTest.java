package com.cvesters.notula.session.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.credential.TestCredential;
import com.cvesters.notula.session.TestSession;
import com.cvesters.notula.session.bdo.SessionAction;
import com.cvesters.notula.user.TestUser;

class SessionCreateDtoTest {

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
	private static final TestUser USER = SESSION.getUser();
	private static final TestCredential CREDENTIAL = TestCredential.EDUARDO_CHRISTIANSEN;

	@Nested
	class ToBdo {

		@Test
		void success() {
			final var dto = new SessionCreateDto(USER.getEmail().value(),
					CREDENTIAL.getPassword().value());

			final SessionAction.Create bdo = dto.toBdo();

			assertThat(bdo.getEmail()).isEqualTo(USER.getEmail());
			assertThat(bdo.getPassword()).isEqualTo(CREDENTIAL.getPassword());
		}

		@Test
		void emailInvalid() {
			final var dto = new SessionCreateDto("invalid",
					CREDENTIAL.getPassword().value());

			assertThatThrownBy(dto::toBdo)
					.isInstanceOf(IllegalArgumentException.class);
		}

		@Test
		void passwordInvalid() {
			final var dto = new SessionCreateDto(USER.getEmail().value(),
					"inv");

			assertThatThrownBy(dto::toBdo)
					.isInstanceOf(IllegalArgumentException.class);
		}
	}

}
