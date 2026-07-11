package com.cvesters.notula.session.bdo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.common.domain.Email;
import com.cvesters.notula.common.domain.Password;
import com.cvesters.notula.credential.TestCredential;
import com.cvesters.notula.user.TestUser;

class SessionActionTest {

	private static final TestUser USER = TestUser.EDUARDO_CHRISTIANSEN;
	private static final TestCredential CREDENTIAL = TestCredential.EDUARDO_CHRISTIANSEN;

	@Nested
	class Create {

		@Test
		void success() {
			final var action = new SessionAction.Create(USER.getEmail(),
					CREDENTIAL.getPassword());

			assertThat(action.getEmail()).isEqualTo(USER.getEmail());
			assertThat(action.getPassword())
					.isEqualTo(CREDENTIAL.getPassword());
		}

		@Test
		void emailNull() {
			final Email email = null;
			final Password password = CREDENTIAL.getPassword();

			assertThatThrownBy(() -> new SessionAction.Create(email, password))
					.isInstanceOf(NullPointerException.class);
		}

		@Test
		void passwordNull() {
			final Email email = USER.getEmail();
			final Password password = null;

			assertThatThrownBy(() -> new SessionAction.Create(email, password))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
