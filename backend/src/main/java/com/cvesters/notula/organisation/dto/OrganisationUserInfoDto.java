package com.cvesters.notula.organisation.dto;

import java.util.Objects;

import com.cvesters.notula.organisation.bdo.OrganisationUserInfo;

public record OrganisationUserInfoDto(long id, long organisationId,
		long userId) {

	public OrganisationUserInfoDto(final OrganisationUserInfo orgUser) {
		Objects.requireNonNull(orgUser);

		final long id = orgUser.getId();
		final long organisationId = orgUser.getOrganisationId();
		final long userId = orgUser.getUserId();
		this(id, organisationId, userId);
	}
}
