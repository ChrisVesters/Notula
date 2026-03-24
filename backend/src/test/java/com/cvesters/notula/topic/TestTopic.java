package com.cvesters.notula.topic;

import java.util.Arrays;
import java.util.List;

import lombok.Getter;

import com.cvesters.notula.meeting.TestMeeting;
import com.cvesters.notula.topic.bdo.TopicAction;
import com.cvesters.notula.topic.bdo.TopicEvent;
import com.cvesters.notula.topic.bdo.TopicInfo;

@Getter
public enum TestTopic {
	SPORER_PROJECT_DELIVERABLES(1, TestMeeting.SPORER_PROJECT, "Deliverables"),
	SPORER_PROJECT_BLOCKERS(2, TestMeeting.SPORER_PROJECT, "Blockers"),
	SPORER_PROJECT_TIMELINE(3, TestMeeting.SPORER_PROJECT, "Timeline"),
	GLOVER_KICKOFF_2026_LOOKBACK(4, TestMeeting.GLOVER_KICKOFF_2026,
			"Looking Back");

	private final long id;
	private final TestMeeting meeting;
	private final String name;

	TestTopic(final long id, final TestMeeting meeting, final String name) {
		this.id = id;
		this.meeting = meeting;
		this.name = name;
	}

	public static List<TestTopic> ofMeeting(final TestMeeting meeting) {
		return Arrays.stream(TestTopic.values())
				.filter(topic -> topic.meeting.equals(meeting))
				.toList();
	}

	public TopicInfo info() {
		return new TopicInfo(id, meeting.getOrganisation().getId(), meeting.getId(), name);
	}

	public TopicAction.Create createAction() {
		return new TopicAction.Create(name);
	}

	public TopicEvent.Create createEvent() {
		return new TopicEvent.Create(id, name);
	}
}
