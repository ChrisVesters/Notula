package com.cvesters.notula.organisation.dto;

import com.cvesters.notula.organisation.bdo.Organisation;

public record OrganisationDto(long id, String name) {
	
	public static OrganisationDto fromBdo(final Organisation organisation) {
		return new OrganisationDto(organisation.getId(), organisation.getName());
	}
}
