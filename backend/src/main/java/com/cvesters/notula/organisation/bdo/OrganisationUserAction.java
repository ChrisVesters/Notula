package com.cvesters.notula.organisation.bdo;

import java.util.Objects;

import lombok.Getter;

import com.cvesters.notula.common.domain.Email;

public sealed interface OrganisationUserAction {

	@Getter
	final class Create implements OrganisationUserAction {
		private final Email email;
		private final OrganisationUserRole role;

		public Create(final Email email, final OrganisationUserRole role) {
			Objects.requireNonNull(email);
			Objects.requireNonNull(role);

			this.email = email;
			this.role = role;
		}
	}

}
