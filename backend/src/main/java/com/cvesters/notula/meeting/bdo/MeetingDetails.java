package com.cvesters.notula.meeting.bdo;

import java.util.Objects;

public record MeetingDetails(MeetingInfo info) {

	public MeetingDetails {
		Objects.requireNonNull(info);
	}

}
