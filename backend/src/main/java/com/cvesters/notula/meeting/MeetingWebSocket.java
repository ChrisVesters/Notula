package com.cvesters.notula.meeting;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import com.cvesters.notula.common.controller.BaseController;
import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.meeting.bdo.MeetingDetails;
import com.cvesters.notula.meeting.dto.MeetingDetailsDto;

@Controller
public class MeetingWebSocket extends BaseController {

	private static final String ENDPOINT = "/meetings/{id}";

	private MeetingDetailsService meetingDetailsService;

	public MeetingWebSocket(final MeetingDetailsService meetingActionService) {
		this.meetingDetailsService = meetingActionService;
	}

	@SubscribeMapping(ENDPOINT)
	public MeetingDetailsDto subscribe(@DestinationVariable final long id) {
		final Principal principal = getPrincipal();

		final MeetingDetails details = meetingDetailsService.get(principal, id);
		return new MeetingDetailsDto(details);
	}
}
