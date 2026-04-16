package com.cvesters.notula.meeting.bdo;

import java.util.Objects;

public record MeetingEvent(long meetingId, MeetingAction action) {

	public MeetingEvent {
		Objects.requireNonNull(action);
	}
}