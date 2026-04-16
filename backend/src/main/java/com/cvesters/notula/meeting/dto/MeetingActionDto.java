package com.cvesters.notula.meeting.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import com.cvesters.notula.meeting.bdo.MeetingAction;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

public final class MeetingActionDto {

	private MeetingActionDto() {
	}

	public static record Create(@NotBlank String name) {

		public MeetingAction.Create toBdo() {
			return new MeetingAction.Create(name);
		}
	}

	@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "action")
	@JsonSubTypes({ @Type(value = Update.Name.class, name = "UPDATE_NAME") })
	public sealed interface Update {

		MeetingAction.Update toBdo();

		public static record Name(@PositiveOrZero int position,
				@PositiveOrZero int length, @NotBlank String value)
				implements Update {

			public MeetingAction.Update toBdo() {
				return new MeetingAction.UpdateName(position, length, value);
			}
		}

	}

}
