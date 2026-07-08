package com.cvesters.notula.organisation.dto;

import jakarta.validation.constraints.NotBlank;

import com.cvesters.notula.common.domain.Email;
import com.cvesters.notula.organisation.bdo.OrganisationUserAction;

public final class OrganisationUserActionDto {

	private OrganisationUserActionDto() {
	}

	public static record Create(
			@NotBlank @jakarta.validation.constraints.Email String email) {

		public OrganisationUserAction.Create toBdo() {
			final var validatedEmail = new Email(email);
			return new OrganisationUserAction.Create(validatedEmail);
		}
	}
}
