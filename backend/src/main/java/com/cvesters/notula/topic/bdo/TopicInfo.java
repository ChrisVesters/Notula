package com.cvesters.notula.topic.bdo;

import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.Validate;

import lombok.Getter;

import com.cvesters.notula.common.domain.Minutes;

@Getter
public class TopicInfo {

	private final Long id;
	private final long organisationId;
	private final long meetingId;
	private int sequenceId;
	private String name;
	private String description;
	private Minutes duration;

	public TopicInfo(final long organisationId, final long meetingId,
			final int sequenceId, final String name) {
		this(null, organisationId, meetingId, sequenceId, name, "", null);
	}

	public TopicInfo(final Long id, final long organisationId,
			final long meetingId, final int sequenceId, final String name,
			final String description, final Minutes duration) {
		Validate.isTrue(sequenceId >= 0);
		Objects.requireNonNull(name);
		Objects.requireNonNull(description);

		this.id = id;
		this.organisationId = organisationId;
		this.meetingId = meetingId;
		this.sequenceId = sequenceId;
		this.name = name;
		this.description = description;
		this.duration = duration;
	}

	public long getId() {
		Validate.validState(id != null);

		return id;
	}

	public Optional<Minutes> getDuration() {
		return Optional.ofNullable(duration);
	}

	public void setSequenceId(final int sequenceId) {
		Validate.isTrue(sequenceId >= 0);

		this.sequenceId = sequenceId;
	}

	public void setName(final String name) {
		Objects.requireNonNull(name);

		this.name = name;
	}

	public void setDescription(final String description) {
		Objects.requireNonNull(description);

		this.description = description;
	}

	public void setDuration(final Minutes duration) {
		this.duration = duration;
	}
}
