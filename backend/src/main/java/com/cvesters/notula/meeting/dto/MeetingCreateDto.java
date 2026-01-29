package com.cvesters.notula.meeting.dto;

import jakarta.validation.constraints.NotBlank;

import com.cvesters.notula.meeting.bdo.MeetingInfo;

public record MeetingCreateDto(@NotBlank String name) {

	public MeetingInfo toBdo(final long organisationId) {
		return new MeetingInfo(organisationId, name);
	}
}
