package com.cvesters.notula.topic.bdo;

import java.util.Objects;

import org.apache.commons.lang3.Validate;

import lombok.Getter;

@Getter
public class TopicInfo {

	private final Long id;
	private final long organisationId;
	private final long meetingId;
	private String name;

	public TopicInfo(final long organisationId, final long meetingId,
			final String name) {
		this(null, organisationId, meetingId, name);
	}

	public TopicInfo(final Long id, final long organisationId,
			final long meetingId, final String name) {
		Validate.notBlank(name);

		this.id = id;
		this.organisationId = organisationId;
		this.meetingId = meetingId;
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
