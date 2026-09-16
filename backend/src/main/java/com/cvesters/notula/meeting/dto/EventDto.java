package com.cvesters.notula.meeting.dto;

import java.util.Objects;

import com.cvesters.notula.common.dto.OriginDto;

public record EventDto(long revision, OriginDto origin,
		MutationDto mutation) {

	public EventDto {
		Objects.requireNonNull(origin);
		Objects.requireNonNull(mutation);
	}
}
