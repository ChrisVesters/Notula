package com.cvesters.notula.meeting.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.cvesters.notula.common.dto.TextUpdateDto;
import com.cvesters.notula.meeting.bdo.MeetingAction;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

public final class MeetingActionDto {

	private MeetingActionDto() {
	}

	public static record Create(@NotNull String name) {

		public MeetingAction.Create toBdo() {
			return new MeetingAction.Create(name);
		}
	}

	@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "action")
	@JsonSubTypes({ @Type(value = Update.Name.class, name = "UPDATE_NAME"),
			@Type(value = Update.Description.class, name = "UPDATE_DESCRIPTION") })
	public abstract static sealed class Update {

		private final long meetingId;

		protected Update(final long meetingId) {
			this.meetingId = meetingId;
		}

		public long getMeetingId() {
			return meetingId;
		}

		public abstract MeetingAction.Update toBdo();

		public static final class Name extends Update {

			@Valid
			private TextUpdateDto update;

			public Name(final long meetingId, final int position,
					final int length, final String value) {
				super(meetingId);

				this.update = new TextUpdateDto(position, length, value);
			}

			public MeetingAction.Update toBdo() {
				final int position = update.position();
				final int length = update.length();
				final String value = update.value();

				return new MeetingAction.UpdateName(position, length, value);
			}
		}

		public static final class Description extends Update {

			@Valid
			private TextUpdateDto update;

			public Description(final long meetingId, final int position,
					final int length, final String value) {
				super(meetingId);

				this.update = new TextUpdateDto(position, length, value);
			}

			public MeetingAction.Update toBdo() {
				final int position = update.position();
				final int length = update.length();
				final String value = update.value();

				return new MeetingAction.UpdateDescription(position, length,
						value);
			}
		}

	}

}
