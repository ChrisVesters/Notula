package com.cvesters.notula.organisation.bdo;

import java.util.Objects;

import lombok.Getter;

import com.cvesters.notula.common.domain.Email;

public sealed interface OrganisationUserAction {

	@Getter
	final class Create implements OrganisationUserAction {
		private final Email email;

		public Create(final Email email) {
			Objects.requireNonNull(email);

			this.email = email;
		}
	}

}
