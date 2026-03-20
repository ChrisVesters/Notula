package com.cvesters.notula.meeting.bdo;

import java.util.List;
import java.util.Objects;

import com.cvesters.notula.topic.bdo.TopicInfo;

public record MeetingDetails(MeetingInfo info, List<TopicInfo> topics) {

	public MeetingDetails {
		Objects.requireNonNull(info);
		Objects.requireNonNull(topics);
	}

}
