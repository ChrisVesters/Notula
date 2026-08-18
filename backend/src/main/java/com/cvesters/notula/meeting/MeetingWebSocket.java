package com.cvesters.notula.meeting;

import jakarta.validation.Valid;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import com.cvesters.notula.common.controller.BaseController;
import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.meeting.bdo.MeetingAction;
import com.cvesters.notula.meeting.dto.MeetingActionDto;

@Controller
public class MeetingWebSocket extends BaseController {

	private static final String ENDPOINT = "/meetings";

	private final MeetingService meetingService;

	public MeetingWebSocket(final MeetingService meetingService) {
		this.meetingService = meetingService;
	}

	@MessageMapping(ENDPOINT + "/update")
	public void update(@Valid @Payload final MeetingActionDto.Update dto) {
		final Principal principal = getPrincipal();

		final long meetingId = dto.getMeetingId();
		final MeetingAction.Update action = dto.toBdo();
		meetingService.update(principal, meetingId, action);
	}
}
