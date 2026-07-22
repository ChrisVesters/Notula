package com.cvesters.notula.organisation.bdo;

import lombok.Getter;

@Getter
public class OrganisationUserInfo {

	private final Long id;
	private final long organisationId;
	private final long userId;
	private final OrganisationUserRole role;

	public OrganisationUserInfo(final long organisationId, final long userId,
			final OrganisationUserRole role) {
		this(null, organisationId, userId, role);
	}

	public OrganisationUserInfo(final Long id, final long organisationId,
			final long userId, final OrganisationUserRole role) {
		this.id = id;
		this.organisationId = organisationId;
		this.userId = userId;
		this.role = role;
	}
}
