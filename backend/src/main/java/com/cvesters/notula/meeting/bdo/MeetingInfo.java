package com.cvesters.notula.meeting.bdo;

import java.util.Objects;

import org.apache.commons.lang3.Validate;

import lombok.Getter;

@Getter
public class MeetingInfo {

	private final Long id;
	private final long organisationId;
	private String name;

	public MeetingInfo(final long organisationId, final String name) {
		this(null, organisationId, name);
	}

	public MeetingInfo(final Long id, final long organisationId, final String name) {
		Objects.requireNonNull(name);

		this.id = id;
		this.organisationId = organisationId;
		this.name = name;
	}

	public long getId() {
		Validate.validState(id != null);

		return id;
	}

	public void setName(final String name) {
		Objects.requireNonNull(name);

		this.name = name;
	}

}
