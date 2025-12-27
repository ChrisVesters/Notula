package com.cvesters.notula.organisation;

import lombok.Getter;

import com.cvesters.notula.organisation.bdo.OrganisationInfo;

@Getter
public enum TestOrganisation {
	SPORER(1L, "Sporer LLC"),
	GLOVER(2L, "Glover Group"),
	HEUL(3L, "Heul");

	private final long id;
	private final String name;

	TestOrganisation(final long id, final String name) {
		this.id = id;
		this.name = name;
	}

	public OrganisationInfo info() {
		return new OrganisationInfo(id, name);
	}
}
