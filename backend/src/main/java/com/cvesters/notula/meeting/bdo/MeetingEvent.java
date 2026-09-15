package com.cvesters.notula.meeting.bdo;

import java.util.Objects;

import com.cvesters.notula.common.domain.Origin;

public record MeetingEvent(MeetingAction action, Origin origin) {

	public MeetingEvent {
		Objects.requireNonNull(action);
		Objects.requireNonNull(origin);
	}
}
