package com.cvesters.notula.meeting;

import jakarta.validation.Valid;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import com.cvesters.notula.common.domain.Origin;
import com.cvesters.notula.meeting.bdo.MeetingAction;
import com.cvesters.notula.meeting.dto.MeetingActionDto;

@Controller
public class MeetingWebSocket {

	private static final String ENDPOINT = "/meetings";

	private final MeetingService meetingService;

	public MeetingWebSocket(final MeetingService meetingService) {
		this.meetingService = meetingService;
	}

	@MessageMapping(ENDPOINT + "/update")
	public void update(final Origin origin,
			@Valid @Payload final MeetingActionDto.Update dto) {
		final long meetingId = dto.getMeetingId();
		final MeetingAction.Update action = dto.toBdo();
		meetingService.update(origin, meetingId, action);
	}
}
