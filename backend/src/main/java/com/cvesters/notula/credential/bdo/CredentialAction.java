package com.cvesters.notula.credential.bdo;

import java.util.Objects;

import lombok.Getter;

import com.cvesters.notula.common.domain.Password;

public sealed interface CredentialAction {

	@Getter
	final class Create implements CredentialAction {

		private final long userId;
		private final Password password;

		public Create(final long userId, final Password password) {
			Objects.requireNonNull(password);

			this.userId = userId;
			this.password = password;
		}
	}

	@Getter
	final class Login implements CredentialAction {

		private final long userId;
		private final Password password;

		public Login(final long userId, final Password password) {
			Objects.requireNonNull(password);

			this.userId = userId;
			this.password = password;
		}
	}

}
