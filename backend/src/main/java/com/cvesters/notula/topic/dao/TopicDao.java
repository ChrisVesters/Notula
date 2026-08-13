package com.cvesters.notula.topic.dao;

import java.util.Objects;
import java.util.Optional;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import org.apache.commons.lang3.Validate;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.cvesters.notula.common.domain.Minutes;
import com.cvesters.notula.topic.bdo.TopicInfo;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Entity(name = "topics")
public class TopicDao {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "organisation_id", nullable = false, updatable = false)
	private long organisationId;

	@Column(name = "meeting_id", nullable = false, updatable = false)
	private long meetingId;

	@Column(name = "sequence_id", nullable = false)
	private int sequenceId;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private String description;

	@Column(nullable = true)
	private Integer duration;

	public TopicDao(final TopicInfo topic) {
		Objects.requireNonNull(topic);

		this.organisationId = topic.getOrganisationId();
		this.meetingId = topic.getMeetingId();
		this.sequenceId = topic.getSequenceId();
		this.name = topic.getName();
		this.description = topic.getDescription();
		this.duration = topic.getDuration()
				.map(Minutes::value)
				.orElse(null);
	}

	public void update(final TopicInfo bdo) {
		Objects.requireNonNull(bdo);

		this.sequenceId = bdo.getSequenceId();
		this.name = bdo.getName();
		this.description = bdo.getDescription();
		this.duration = bdo.getDuration()
				.map(Minutes::value)
				.orElse(null);
	}

	public TopicInfo toBdo() {
		Validate.validState(id != null);

		final var d = Optional.ofNullable(duration)
				.map(Minutes::new)
				.orElse(null);

		return new TopicInfo(id, organisationId, meetingId, sequenceId, name,
				description, d);
	}
}
