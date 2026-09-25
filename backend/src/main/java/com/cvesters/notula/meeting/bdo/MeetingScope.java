package com.cvesters.notula.meeting.bdo;

import java.util.UUID;

public record MeetingScope(long meetingId, long revision, UUID changeId) {

	public MeetingScope(final long meetingId, final long revision) {
		this(meetingId, revision, null);
	}
}
