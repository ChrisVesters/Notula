package com.cvesters.notula.organisation.dto;

import java.util.Objects;

import com.cvesters.notula.organisation.bdo.OrganisationUserInfo;

public record OrganisationUserInfoDto(long id, long organisationId, long userId,
		OrganisationUserRoleDto role) {

	public OrganisationUserInfoDto(final OrganisationUserInfo orgUser) {
		Objects.requireNonNull(orgUser);

		final long id = orgUser.getId();
		final long organisationId = orgUser.getOrganisationId();
		final long userId = orgUser.getUserId();
		final var role = new OrganisationUserRoleDto(orgUser.getRole());

		this(id, organisationId, userId, role);
	}
}
