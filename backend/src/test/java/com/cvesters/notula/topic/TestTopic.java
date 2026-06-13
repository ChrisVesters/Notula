package com.cvesters.notula.topic;

import java.util.Arrays;
import java.util.List;

import lombok.Getter;

import com.cvesters.notula.meeting.TestMeeting;
import com.cvesters.notula.organisation.TestOrganisation;
import com.cvesters.notula.topic.bdo.TopicInfo;

@Getter
public enum TestTopic {
	SPORER_PROJECT_DELIVERABLES(1, TestMeeting.SPORER_PROJECT, "Deliverables",
			"What needs to be done for the project"),
	SPORER_PROJECT_BLOCKERS(2, TestMeeting.SPORER_PROJECT, "Blockers",
			"What is blocking us right now"),
	SPORER_PROJECT_TIMELINE(3, TestMeeting.SPORER_PROJECT, "Timeline",
			"How can we get this organised"),
	GLOVER_KICKOFF_2026_LOOKBACK(4, TestMeeting.GLOVER_KICKOFF_2026,
			"Looking Back",
			"What went well and what can be improved from last year");

	private final long id;
	private final TestMeeting meeting;
	private final String name;
	private final String description;

	TestTopic(final long id, final TestMeeting meeting, final String name,
			final String description) {
		this.id = id;
		this.meeting = meeting;
		this.name = name;
		this.description = description;
	}

	public static List<TestTopic> ofMeeting(final TestMeeting meeting) {
		return Arrays.stream(TestTopic.values())
				.filter(topic -> topic.meeting.equals(meeting))
				.toList();
	}

	public TopicInfo info() {
		return new TopicInfo(id, meeting.getOrganisation().getId(),
				meeting.getId(), name, description);
	}

	public TestOrganisation getOrganisation() {
		return meeting.getOrganisation();
	}
}
