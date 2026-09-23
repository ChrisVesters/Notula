package com.cvesters.notula.topic.bdo;

import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.Validate;

import lombok.Getter;

import com.cvesters.notula.common.domain.Minutes;
import com.cvesters.notula.common.domain.Rank;

@Getter
public class TopicInfo {

	private final Long id;
	private final long organisationId;
	private final long meetingId;
	private Rank rank;
	private String name;
	private String description;
	private Minutes duration;

	public TopicInfo(final long organisationId, final long meetingId,
			final Rank rank, final String name) {
		this(null, organisationId, meetingId, rank, name, "", null);
	}

	public TopicInfo(final Long id, final long organisationId,
			final long meetingId, final Rank rank, final String name,
			final String description, final Minutes duration) {
		Objects.requireNonNull(rank);
		Objects.requireNonNull(name);
		Objects.requireNonNull(description);

		this.id = id;
		this.organisationId = organisationId;
		this.meetingId = meetingId;
		this.rank = rank;
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

	public void setRank(final Rank rank) {
		Objects.requireNonNull(rank);

		this.rank = rank;
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
