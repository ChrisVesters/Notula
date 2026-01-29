package com.cvesters.notula.meeting.dto;

import java.util.Objects;

import com.cvesters.notula.meeting.bdo.MeetingInfo;

public record MeetingInfoDto(long id, String name) {

	public MeetingInfoDto(final MeetingInfo meeting) {
		Objects.requireNonNull(meeting);

		final long id = meeting.getId();
		final String name = meeting.getName();
		this(id, name);
	}
	
}
