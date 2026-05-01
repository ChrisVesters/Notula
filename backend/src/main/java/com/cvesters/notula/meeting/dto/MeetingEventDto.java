package com.cvesters.notula.meeting.dto;

import java.util.Objects;

import lombok.Getter;

import com.cvesters.notula.meeting.bdo.MeetingAction;
import com.cvesters.notula.meeting.bdo.MeetingEvent;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@Getter
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
		@Type(value = MeetingEventDto.Create.class, name = "MEETING_CREATE"),
		@Type(value = MeetingEventDto.UpdateName.class, name = "MEETING_UPDATE_NAME") })
public sealed class MeetingEventDto {

	private final long meetingId;

	protected MeetingEventDto(final long meetingId) {
		this.meetingId = meetingId;
	}

	public static MeetingEventDto of(final MeetingEvent event) {
		Objects.requireNonNull(event);

		return switch (event.action()) {
			case MeetingAction.Create create -> new Create(event.meetingId(),
					create);
			case MeetingAction.UpdateName updateName -> new UpdateName(
					event.meetingId(), updateName);
		};
	}

	@Getter
	public static final class Create extends MeetingEventDto {

		private final String name;

		private Create(final long meetingId,
				final MeetingAction.Create create) {
			super(meetingId);

			this.name = create.getName();
		}
	}

	@Getter
	public static final class UpdateName extends MeetingEventDto {

		private final int position;
		private final int length;
		private final String value;

		private UpdateName(final long meetingId,
				final MeetingAction.UpdateName action) {
			super(meetingId);

			this.position = action.getPosition();
			this.length = action.getLength();
			this.value = action.getValue();
		}
	}
}
