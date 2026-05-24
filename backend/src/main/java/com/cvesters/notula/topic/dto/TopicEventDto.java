package com.cvesters.notula.topic.dto;

import java.util.Objects;

import lombok.Getter;

import com.cvesters.notula.topic.bdo.TopicEvent;

@Getter
public class TopicEventDto {

	private final long topicId;
	private final TopicMutationDto mutation;

	public TopicEventDto(final TopicEvent event) {
		Objects.requireNonNull(event);

		this.topicId = event.topicId();
		this.mutation = TopicMutationDto.of(event.action());
	}

	public String getTarget() {
		return "TOPIC";
	}

}
