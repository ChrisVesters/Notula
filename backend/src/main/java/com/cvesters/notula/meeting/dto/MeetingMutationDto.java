package com.cvesters.notula.meeting.dto;

import java.util.Objects;

import lombok.Getter;

import com.cvesters.notula.meeting.bdo.MeetingAction;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "action")
@JsonSubTypes({ @Type(value = MeetingMutationDto.Create.class, name = "CREATE"),
		@Type(value = MeetingMutationDto.UpdateName.class, name = "UPDATE_NAME"),
		@Type(value = MeetingMutationDto.UpdateDescription.class, name = "UPDATE_DESCRIPTION"),
		@Type(value = MeetingMutationDto.Delete.class, name = "DELETE") })
public sealed interface MeetingMutationDto {

	static MeetingMutationDto of(final MeetingAction action) {
		Objects.requireNonNull(action);

		return switch (action) {
			case MeetingAction.Create create -> new Create(create);
			case MeetingAction.UpdateName updateName -> new UpdateName(
					updateName);
			case MeetingAction.UpdateDescription updateDescription -> new UpdateDescription(
					updateDescription);
			case MeetingAction.Delete _ -> new Delete();
		};
	}

	@Getter
	final class Create implements MeetingMutationDto {

		private final String name;

		private Create(final MeetingAction.Create create) {
			this.name = create.getName();
		}
	}

	@Getter
	final class UpdateName implements MeetingMutationDto {

		private final int position;
		private final int length;
		private final String value;

		private UpdateName(final MeetingAction.UpdateName action) {
			this.position = action.getPosition();
			this.length = action.getLength();
			this.value = action.getValue();
		}
	}

	@Getter
	final class UpdateDescription implements MeetingMutationDto {

		private final int position;
		private final int length;
		private final String value;

		private UpdateDescription(
				final MeetingAction.UpdateDescription action) {
			this.position = action.getPosition();
			this.length = action.getLength();
			this.value = action.getValue();
		}
	}

	@Getter
	final class Delete implements MeetingMutationDto {
	}
}
