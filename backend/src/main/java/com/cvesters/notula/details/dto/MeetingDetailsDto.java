package com.cvesters.notula.details.dto;

import java.util.List;
import java.util.Objects;

import lombok.Getter;

import com.cvesters.notula.details.bdo.MeetingDetails;

@Getter
public class MeetingDetailsDto {

	private final long id;
	private final String name;
	private final String description;

	private final List<TopicDetailsDto> topics;

	public MeetingDetailsDto(final MeetingDetails details) {
		Objects.requireNonNull(details);

		this.id = details.getId();
		this.name = details.getName();
		this.description = details.getDescription();
		this.topics = details.getTopics()
				.stream()
				.map(TopicDetailsDto::new)
				.toList();
	}
}
