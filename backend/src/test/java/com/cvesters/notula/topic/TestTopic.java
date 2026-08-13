package com.cvesters.notula.topic;

import java.util.Arrays;
import java.util.List;

import lombok.Getter;

import com.cvesters.notula.common.domain.Minutes;
import com.cvesters.notula.meeting.TestMeeting;
import com.cvesters.notula.organisation.TestOrganisation;
import com.cvesters.notula.topic.bdo.TopicInfo;

@Getter
public enum TestTopic {
	SPORER_PROJECT_DELIVERABLES(1, TestMeeting.SPORER_PROJECT, 0,
			"Deliverables", "What needs to be done for the project",
			new Minutes(30)),
	SPORER_PROJECT_BLOCKERS(2, TestMeeting.SPORER_PROJECT, 1, "Blockers",
			"What is blocking us right now", new Minutes(15)),
	SPORER_PROJECT_TIMELINE(3, TestMeeting.SPORER_PROJECT, 2, "Timeline",
			"How can we get this organised", null),
	GLOVER_KICKOFF_2026_LOOKBACK(4, TestMeeting.GLOVER_KICKOFF_2026, 0,
			"Looking Back",
			"What went well and what can be improved from last year",
			new Minutes(45));

	private final long id;
	private final TestMeeting meeting;
	private final int sequenceId;
	private final String name;
	private final String description;
	private final Minutes duration;

	TestTopic(final long id, final TestMeeting meeting, final int sequenceId,
			final String name, final String description,
			final Minutes duration) {
		this.id = id;
		this.meeting = meeting;
		this.sequenceId = sequenceId;
		this.name = name;
		this.description = description;
		this.duration = duration;
	}

	public static List<TestTopic> ofMeeting(final TestMeeting meeting) {
		return Arrays.stream(TestTopic.values())
				.filter(topic -> topic.meeting.equals(meeting))
				.toList();
	}

	public TopicInfo info() {
		return new TopicInfo(id, meeting.getOrganisation().getId(),
				meeting.getId(), sequenceId, name, description, duration);
	}

	public TestOrganisation getOrganisation() {
		return meeting.getOrganisation();
	}
}
