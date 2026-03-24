package com.cvesters.notula.topic.dto;

import java.util.Objects;

import com.cvesters.notula.topic.bdo.TopicEvent;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
		@Type(value = TopicEventDto.Create.class, name = "TOPIC_CREATE") })
public sealed interface TopicEventDto {

	public static TopicEventDto of(final TopicEvent event) {
		return switch (event) {
			case TopicEvent.Create create -> new Create(create);
		};
	}

	public static record Create(long id, String name) implements TopicEventDto {

		public Create(final TopicEvent.Create event) {
			Objects.requireNonNull(event);

			this(event.id(), event.name());
		}
	}
}
