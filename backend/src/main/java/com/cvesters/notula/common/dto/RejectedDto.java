package com.cvesters.notula.common.dto;

import java.util.Objects;
import java.util.UUID;

import com.cvesters.notula.common.domain.ChangeId;

public record RejectedDto(UUID id, boolean retryable, String reason) {

	public static RejectedDto permanent(final ChangeId changeId,
			final String reason) {
		Objects.requireNonNull(changeId);

		return new RejectedDto(changeId.value(), false, reason);
	}

	public static RejectedDto temporary(final ChangeId changeId,
			final String reason) {
		Objects.requireNonNull(changeId);

		return new RejectedDto(changeId.value(), true, reason);
	}
}
