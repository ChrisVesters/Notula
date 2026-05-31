package com.cvesters.notula.meeting.dto;

import java.util.Objects;

import com.cvesters.notula.meeting.bdo.MeetingInfo;

public record MeetingInfoDto(long id, String name, String description) {

	public MeetingInfoDto(final MeetingInfo meeting) {
		Objects.requireNonNull(meeting);

		final long id = meeting.getId();
		final String name = meeting.getName();
		final String description = meeting.getDescription();

		this(id, name, description);
	}
	
}
