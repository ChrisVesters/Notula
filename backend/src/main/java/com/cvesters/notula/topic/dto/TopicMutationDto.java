package com.cvesters.notula.topic.dto;

import java.util.Objects;

import lombok.Getter;

import com.cvesters.notula.topic.bdo.TopicAction;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "action")
@JsonSubTypes({ @Type(value = TopicMutationDto.Create.class, name = "CREATE"),
		@Type(value = TopicMutationDto.UpdateName.class, name = "UPDATE_NAME"),
		@Type(value = TopicMutationDto.UpdateDescription.class, name = "UPDATE_DESCRIPTION"),
		@Type(value = TopicMutationDto.Delete.class, name = "DELETE") })
public sealed interface TopicMutationDto {

	static TopicMutationDto of(final TopicAction action) {
		Objects.requireNonNull(action);

		return switch (action) {
			case TopicAction.Create create -> new Create(create);
			case TopicAction.UpdateName updateName -> new UpdateName(
					updateName);
			case TopicAction.UpdateDescription updateDescription -> new UpdateDescription(
					updateDescription);
			case TopicAction.Delete _ -> new Delete();
		};
	}

	@Getter
	final class Create implements TopicMutationDto {

		private final String name;

		private Create(final TopicAction.Create create) {
			this.name = create.getName();
		}
	}

	@Getter
	final class UpdateName implements TopicMutationDto {

		private final int position;
		private final int length;
		private final String value;

		private UpdateName(final TopicAction.UpdateName action) {
			this.position = action.getPosition();
			this.length = action.getLength();
			this.value = action.getValue();
		}
	}

	@Getter
	final class UpdateDescription implements TopicMutationDto {

		private final int position;
		private final int length;
		private final String value;

		private UpdateDescription(final TopicAction.UpdateDescription action) {
			this.position = action.getPosition();
			this.length = action.getLength();
			this.value = action.getValue();
		}
	}

	@Getter
	final class Delete implements TopicMutationDto {

		private Delete() {
		}
	}
}
