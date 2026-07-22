package com.cvesters.notula.organisation.dto;

import jakarta.validation.constraints.NotBlank;

import com.cvesters.notula.common.domain.Email;
import com.cvesters.notula.organisation.bdo.OrganisationUserAction;
import com.cvesters.notula.organisation.bdo.OrganisationUserRole;

public final class OrganisationUserActionDto {

	private OrganisationUserActionDto() {
	}

	public static record Create(
			@NotBlank @jakarta.validation.constraints.Email String email,
			OrganisationUserRoleDto role) {

		public OrganisationUserAction.Create toBdo() {
			final var validatedEmail = new Email(email);
			final OrganisationUserRole validatedRole = role.toBdo();
			return new OrganisationUserAction.Create(validatedEmail,
					validatedRole);
		}
	}
}
