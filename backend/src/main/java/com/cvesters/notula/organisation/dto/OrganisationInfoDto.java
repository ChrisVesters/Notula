package com.cvesters.notula.organisation.dto;

import java.util.Objects;

import com.cvesters.notula.organisation.bdo.OrganisationInfo;

public record OrganisationInfoDto(long id, String name) {

	public OrganisationInfoDto(final OrganisationInfo organisation) {
		Objects.requireNonNull(organisation);

		final long id = organisation.getId();
		final String name = organisation.getName();
		this(id, name);
	}
}
