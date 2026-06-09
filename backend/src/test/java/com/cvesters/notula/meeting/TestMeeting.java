package com.cvesters.notula.meeting;

import lombok.Getter;

import com.cvesters.notula.meeting.bdo.MeetingInfo;
import com.cvesters.notula.organisation.TestOrganisation;

@Getter
public enum TestMeeting {
	SPORER_PROJECT(1, TestOrganisation.SPORER, "Project Meeting",
			"Discuss project progress and next steps"),
	SPORER_RETRO(2, TestOrganisation.SPORER, "Retrospective Meeting",
			"Reflect on the past sprint and identify improvements"),
	GLOVER_KICKOFF_2026(3, TestOrganisation.GLOVER, "2026 Kickoff Meeting",
			"What are our goals and expectations for 2026?"),
	SPORER_Q2_PLANNING(4, TestOrganisation.SPORER, "Q2 Planning Session", "");

	private final long id;
	private final TestOrganisation organisation;
	private final String name;
	private final String description;

	TestMeeting(final long id, final TestOrganisation organisation,
			final String name, final String description) {
		this.id = id;
		this.organisation = organisation;
		this.name = name;
		this.description = description;
	}

	public MeetingInfo info() {
		return new MeetingInfo(id, organisation.getId(), name, description);
	}

}
