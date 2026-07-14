package com.cvesters.notula.topic.bdo;

import java.util.Objects;

import org.apache.commons.lang3.Validate;

import lombok.Getter;

@Getter
public class TopicInfo {

	private final Long id;
	private final long organisationId;
	private final long meetingId;
	private int sequenceId;
	private String name;
	private String description;

	public TopicInfo(final long organisationId, final long meetingId,
			final int sequenceId, final String name) {
		this(null, organisationId, meetingId, sequenceId, name, "");
	}

	public TopicInfo(final Long id, final long organisationId,
			final long meetingId, final int sequenceId, final String name,
			final String description) {
		Validate.isTrue(sequenceId >= 0);
		Objects.requireNonNull(name);
		Objects.requireNonNull(description);

		this.id = id;
		this.organisationId = organisationId;
		this.meetingId = meetingId;
		this.sequenceId = sequenceId;
		this.name = name;
		this.description = description;
	}

	public long getId() {
		Validate.validState(id != null);

		return id;
	}

	public void moveUp() {
		Validate.validState(sequenceId > 0);

		sequenceId--;
	}

	public void moveDown() {
		Validate.validState(sequenceId < Integer.MAX_VALUE);

		sequenceId++;
	}

	public void setName(final String name) {
		Objects.requireNonNull(name);

		this.name = name;
	}

	public void setDescription(final String description) {
		Objects.requireNonNull(description);

		this.description = description;
	}
}
