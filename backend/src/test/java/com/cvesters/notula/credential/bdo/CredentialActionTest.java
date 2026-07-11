package com.cvesters.notula.credential.bdo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.common.domain.Password;

class CredentialActionTest {

	@Nested
	class Create {

		@Test
		void success() {
			final var password = new Password("password");

			final var action = new CredentialAction.Create(1L, password);

			assertThat(action.getUserId()).isEqualTo(1L);
			assertThat(action.getPassword()).isEqualTo(password);
		}

		@Test
		void passwordNull() {
			assertThatThrownBy(() -> new CredentialAction.Create(1L, null))
					.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class Login {

		@Test
		void success() {
			final var password = new Password("password");

			final var action = new CredentialAction.Login(1L, password);

			assertThat(action.getUserId()).isEqualTo(1L);
			assertThat(action.getPassword()).isEqualTo(password);
		}

		@Test
		void passwordNull() {
			assertThatThrownBy(() -> new CredentialAction.Login(1L, null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
