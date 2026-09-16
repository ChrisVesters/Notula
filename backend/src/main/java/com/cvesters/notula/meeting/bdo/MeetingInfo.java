package com.cvesters.notula.meeting.bdo;

import java.util.Objects;

import org.apache.commons.lang3.Validate;

import lombok.Getter;

@Getter
public class MeetingInfo {

	private final Long id;
	private final long organisationId;
	private String name;
	private String description;
	private long revision;

	public MeetingInfo(final long organisationId, final String name) {
		this(null, organisationId, name, "", 0);
	}

	public MeetingInfo(final Long id, final long organisationId,
			final String name, final String description,
			final long revision) {
		Objects.requireNonNull(name);
		Objects.requireNonNull(description);

		this.id = id;
		this.organisationId = organisationId;
		this.name = name;
		this.description = description;
		this.revision = revision;
	}

	public long getId() {
		Validate.validState(id != null);

		return id;
	}

	public void setName(final String name) {
		Objects.requireNonNull(name);

		this.name = name;
	}

	public void setDescription(final String description) {
		Objects.requireNonNull(description);

		this.description = description;
	}

	public void bumpRevision() {
		revision++;
	}

}
