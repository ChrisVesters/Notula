package com.cvesters.notula.meeting.bdo;

import org.apache.commons.lang3.Validate;

import lombok.Getter;

@Getter
public class MeetingInfo {

	private final Long id;
	private final long organisationId;
	private final String name;

	public MeetingInfo(final long organisationId, final String name) {
		this(null, organisationId, name);
	}

	public MeetingInfo(final Long id, final long organisationId, final String name) {
		Validate.notBlank(name);

		this.id = id;
		this.organisationId = organisationId;
		this.name = name;
	}

	public long getId() {
		Validate.validState(id != null);

		return id;
	}
}
