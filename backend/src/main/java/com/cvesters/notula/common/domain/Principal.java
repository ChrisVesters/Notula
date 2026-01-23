package com.cvesters.notula.common.domain;

import org.apache.commons.lang3.Validate;

public record Principal(long userId, Long organisationId) {

	public Principal(final long userId) {
		this(userId, null);
	}

	public Long organisationId() {
		Validate.validState(organisationId != null);

		return organisationId;
	}

}
