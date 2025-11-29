package com.cvesters.notula.organisation.bdo;

import lombok.Getter;

@Getter
public class OrganisationUserInfo {

	private final Long id;
	private final long organisationId;
	private final long userId;

	public OrganisationUserInfo(final long organisationId, final long userId) {
		this(null, organisationId, userId);
	}

	public OrganisationUserInfo(final Long id, final long organisationId,
			final long userId) {
		this.id = id;
		this.organisationId = organisationId;
		this.userId = userId;
	}
}
