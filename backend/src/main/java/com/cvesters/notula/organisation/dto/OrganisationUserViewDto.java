package com.cvesters.notula.organisation.dto;

import java.util.Objects;

import com.cvesters.notula.organisation.bdo.OrganisationUserView;

public record OrganisationUserViewDto(long id, long organisationId, long userId,
		String email, OrganisationUserRoleDto role) {

	public OrganisationUserViewDto(final OrganisationUserView user) {
		Objects.requireNonNull(user);

		final long id = user.getId();
		final long organisationId = user.getOrganisationId();
		final long userId = user.getUserId();
		final String email = user.getEmail();
		final var role = new OrganisationUserRoleDto(user.getRole());

		this(id, organisationId, userId, email, role);
	}
}
