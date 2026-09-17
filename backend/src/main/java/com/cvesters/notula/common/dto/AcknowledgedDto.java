package com.cvesters.notula.common.dto;

import java.util.Objects;
import java.util.UUID;

import com.cvesters.notula.common.domain.ChangeId;

public record AcknowledgedDto(UUID id, long revision) {

	public static AcknowledgedDto of(final ChangeId changeId,
			final long revision) {
		Objects.requireNonNull(changeId);

		return new AcknowledgedDto(changeId.value(), revision);
	}
}
