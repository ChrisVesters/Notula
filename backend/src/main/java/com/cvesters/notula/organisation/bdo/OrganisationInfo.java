package com.cvesters.notula.organisation.bdo;

import java.util.Objects;

import org.apache.commons.lang3.Validate;

import lombok.Getter;

@Getter
public class OrganisationInfo {

	private final Long id;
	private final String name;

	public OrganisationInfo(final String name) {
		this(null, name);
	}

	public OrganisationInfo(final Long id, final String name) {
		Objects.requireNonNull(name);
		Validate.notBlank(name);

		this.id = id;
		this.name = name;
	}

	public long getId() {
		Validate.validState(id != null);

		return id;
	}	
}
