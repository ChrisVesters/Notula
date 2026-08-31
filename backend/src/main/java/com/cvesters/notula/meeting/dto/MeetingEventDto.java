package com.cvesters.notula.meeting.dto;

import java.util.Objects;

import lombok.Getter;

import com.cvesters.notula.common.dto.OriginDto;
import com.cvesters.notula.meeting.bdo.MeetingEvent;

@Getter
public class MeetingEventDto {

	private final long meetingId;
	private final MeetingMutationDto mutation;
	private final OriginDto origin;

	public MeetingEventDto(final MeetingEvent event) {
		Objects.requireNonNull(event);

		this.meetingId = event.meetingId();
		this.mutation = MeetingMutationDto.of(event.action());
		this.origin = new OriginDto(event.origin());
	}

	public String getTarget() {
		return "MEETING";
	}
}
