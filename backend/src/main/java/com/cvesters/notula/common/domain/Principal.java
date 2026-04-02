package com.cvesters.notula.common.domain;

import org.apache.commons.lang3.Validate;
import org.springframework.security.core.AuthenticatedPrincipal;

public record Principal(long userId, Long organisationId) implements AuthenticatedPrincipal {

	public Principal(final long userId) {
		this(userId, null);
	}

	public Long organisationId() {
		Validate.validState(organisationId != null);

		return organisationId;
	}

	@Override
	public String getName() {
		return String.valueOf(userId);
	}
}
