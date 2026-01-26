package com.cvesters.notula.meeting;

import lombok.Getter;

import com.cvesters.notula.meeting.bdo.MeetingInfo;
import com.cvesters.notula.organisation.TestOrganisation;

@Getter
public enum TestMeeting {
	SPORER_PROJECT(1, TestOrganisation.SPORER, "Project Meeting"),
	SPORER_RETRO(2, TestOrganisation.SPORER, "Retrospective Meeting"),
	GLOVER_KICKOFF_2026(3, TestOrganisation.GLOVER, "2026 Kickoff Meeting"),
	SPORER_Q2_PLANNING(4, TestOrganisation.SPORER, "Q2 Planning Session");

	private final long id;
	private final TestOrganisation organisation;
	private final String name;

	TestMeeting(final long id, final TestOrganisation organisation, final String name) {
		this.id = id;
		this.organisation = organisation;
		this.name = name;
	}

	public MeetingInfo info() {
		return new MeetingInfo(id, name);
	}

}
