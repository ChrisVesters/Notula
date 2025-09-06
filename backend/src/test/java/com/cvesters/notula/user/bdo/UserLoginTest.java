package com.cvesters.notula.user.bdo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.user.TestUser;

class UserLoginTest {

	private static final TestUser USER = TestUser.EDUARDO_CHRISTIANSEN;

	@Nested
	class Constructor {

		@Test
		void success() {
			final var login = new UserLogin(USER.getEmail(),
					USER.getPassword());

			assertThat(login.getEmail()).isEqualTo(USER.getEmail());
			assertThat(login.getPassword()).isEqualTo(USER.getPassword());
		}

		@Test
		void emailNull() {
			final String password = USER.getPassword();
			assertThatThrownBy(() -> new UserLogin(null, password))
					.isInstanceOf(NullPointerException.class);
		}

		@Test
		void passwordNull() {
			final String email = USER.getEmail();
			assertThatThrownBy(() -> new UserLogin(email, null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
