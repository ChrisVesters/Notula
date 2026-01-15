package com.cvesters.notula.common.domain;

import java.util.Optional;

import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public class Principal {

	private final long userId;
	private final Long organisationId;

	public Principal(final long userId) {
		this(userId, null);
	}

	public Principal(final long userId, final Long organisationId) {
		this.userId = userId;
		this.organisationId = organisationId;
	}

	public Optional<Long> organisationId() {
		return Optional.ofNullable(organisationId);
	}

}
