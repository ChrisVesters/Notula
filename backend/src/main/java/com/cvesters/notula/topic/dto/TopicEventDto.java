package com.cvesters.notula.topic.dto;

import java.util.Objects;

import lombok.Getter;

import com.cvesters.notula.topic.bdo.TopicAction;
import com.cvesters.notula.topic.bdo.TopicEvent;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@Getter
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
		@Type(value = TopicEventDto.Create.class, name = "TOPIC_CREATE"),
		@Type(value = TopicEventDto.UpdateName.class, name = "TOPIC_UPDATE_NAME") })
public sealed class TopicEventDto {

	private final long topicId;

	protected TopicEventDto(final long topicId) {
		this.topicId = topicId;
	}

	public static TopicEventDto of(final TopicEvent event) {
		Objects.requireNonNull(event);

		return switch (event.action()) {
			case TopicAction.Create create -> new Create(event.topicId(),
					create);
			case TopicAction.UpdateName updateName -> new UpdateName(
					event.topicId(), updateName);
		};
	}

	@Getter
	public static final class Create extends TopicEventDto {

		private final String name;

		private Create(final long topicId, final TopicAction.Create create) {
			super(topicId);

			this.name = create.getName();
		}
	}

	@Getter
	public static final class UpdateName extends TopicEventDto {

		private final int position;
		private final int length;
		private final String value;

		private UpdateName(final long topicId,
				final TopicAction.UpdateName action) {
			super(topicId);

			this.position = action.getPosition();
			this.length = action.getLength();
			this.value = action.getValue();
		}
	}
}
