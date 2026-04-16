package com.cvesters.notula.meeting;

import jakarta.validation.Valid;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import com.cvesters.notula.common.controller.BaseController;
import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.meeting.bdo.MeetingAction;
import com.cvesters.notula.meeting.bdo.MeetingDetails;
import com.cvesters.notula.meeting.dto.MeetingActionDto;
import com.cvesters.notula.meeting.dto.MeetingDetailsDto;

@Controller
public class MeetingWebSocket extends BaseController {

	private static final String ENDPOINT = "/meetings/{id}";

	private final MeetingDetailsService meetingDetailsService;
	private final MeetingService meetingService;

	public MeetingWebSocket(final MeetingDetailsService meetingActionService,
			final MeetingService meetingService) {
		this.meetingDetailsService = meetingActionService;
		this.meetingService = meetingService;
	}

	@SubscribeMapping(ENDPOINT)
	public MeetingDetailsDto subscribe(@DestinationVariable final long id) {
		final Principal principal = getPrincipal();

		final MeetingDetails details = meetingDetailsService.get(principal, id);
		return new MeetingDetailsDto(details);
	}

	@MessageMapping(ENDPOINT)
	public void update(@DestinationVariable final long id,
			@Valid @Payload final MeetingActionDto.Update dto) {
		final Principal principal = getPrincipal();

		final MeetingAction.Update action = dto.toBdo();
		meetingService.update(principal, id, action);
	}
}
