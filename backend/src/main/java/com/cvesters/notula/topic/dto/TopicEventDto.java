package com.cvesters.notula.topic.dto;

import java.util.Objects;

import lombok.Getter;

import com.cvesters.notula.common.dto.OriginDto;
import com.cvesters.notula.topic.bdo.TopicEvent;

@Getter
public class TopicEventDto {

	private final long topicId;
	private final TopicMutationDto mutation;
	private final OriginDto origin;

	public TopicEventDto(final TopicEvent event) {
		Objects.requireNonNull(event);

		this.topicId = event.topic().getId();
		this.mutation = TopicMutationDto.of(event.action());
		this.origin = new OriginDto(event.origin());
	}

	public String getTarget() {
		return "TOPIC";
	}

}
