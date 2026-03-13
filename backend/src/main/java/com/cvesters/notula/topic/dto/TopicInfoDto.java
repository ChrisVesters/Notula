package com.cvesters.notula.topic.dto;

import java.util.Objects;

import com.cvesters.notula.topic.bdo.TopicInfo;

public record TopicInfoDto(long id, String name) {

	public TopicInfoDto(final TopicInfo topic) {
		Objects.requireNonNull(topic);

		final long id = topic.getId();
		final String name = topic.getName();
		
		this(id, name);
	}
}
