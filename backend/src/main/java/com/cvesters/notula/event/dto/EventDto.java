package com.cvesters.notula.event.dto;

import java.util.Objects;

import com.cvesters.notula.common.dto.OriginDto;
import com.cvesters.notula.event.bdo.EventInfo;

public record EventDto(long revision, OriginDto origin,
		MutationDto mutation) {

	public EventDto {
		Objects.requireNonNull(origin);
		Objects.requireNonNull(mutation);
	}

	public EventDto(final EventInfo event) {
		Objects.requireNonNull(event);

		final var origin = new OriginDto(event.getUserId(),
				event.getClientId());
		final MutationDto mutation = MutationDto.of(event.getMutation());

		this(event.getRevision(), origin, mutation);
	}
}
