package com.cvesters.notula.organisation.dto;

import java.util.Objects;

import com.cvesters.notula.organisation.bdo.OrganisationUserInfo;

public record OrganisationUserInfoDto(long id, long organisationId,
		long userId) {

	public OrganisationUserInfoDto(final OrganisationUserInfo organisation) {
		Objects.requireNonNull(organisation);

		final long id = organisation.getId();
		final long organisationId = organisation.getOrganisationId();
		final long userId = organisation.getUserId();
		this(id, organisationId, userId);
	}
}
