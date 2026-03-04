package com.cvesters.notula.meeting.dto;

import java.util.Objects;

import com.cvesters.notula.meeting.bdo.MeetingDetails;

public record MeetingDetailsDto(MeetingInfoDto info) {

	public MeetingDetailsDto(final MeetingDetails details) {
		Objects.requireNonNull(details);

		this(new MeetingInfoDto(details.info()));
	}
}
