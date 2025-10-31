package com.cvesters.notula.session.bdo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.common.domain.Email;
import com.cvesters.notula.common.domain.Password;
import com.cvesters.notula.user.TestUser;

class SessionLoginTest {

	private static final TestUser USER = TestUser.EDUARDO_CHRISTIANSEN;

	@Nested
	class Constructor {

		@Test
		void success() {
			final var login = new SessionLogin(USER.getEmail(),
					USER.getPassword());

			assertThat(login.getEmail()).isEqualTo(USER.getEmail());
			assertThat(login.getPassword()).isEqualTo(USER.getPassword());
		}

		@Test
		void emailNull() {
			final Password password = USER.getPassword();
			assertThatThrownBy(() -> new SessionLogin(null, password))
					.isInstanceOf(NullPointerException.class);
		}

		@Test
		void passwordNull() {
			final Email email = USER.getEmail();
			assertThatThrownBy(() -> new SessionLogin(email, null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
