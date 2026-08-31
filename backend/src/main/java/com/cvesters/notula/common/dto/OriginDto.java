package com.cvesters.notula.common.dto;

import java.util.Objects;
import java.util.UUID;

import com.cvesters.notula.common.domain.Origin;

public record OriginDto(long userId, UUID clientId) {

	public OriginDto(final Origin origin) {
		Objects.requireNonNull(origin);

		final long userId = origin.userId();
		final UUID clientId = origin.clientId();

		this(userId, clientId);
	}
}
