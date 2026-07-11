package com.cvesters.notula.session.bdo;

import java.util.Objects;

import lombok.Getter;

import com.cvesters.notula.common.domain.Email;
import com.cvesters.notula.common.domain.Password;

public sealed interface SessionAction {

	@Getter
	final class Create implements SessionAction {
		private final Email email;
		private final Password password;

		public Create(final Email email, final Password password) {
			Objects.requireNonNull(email);
			Objects.requireNonNull(password);

			this.email = email;
			this.password = password;
		}
	}

}
