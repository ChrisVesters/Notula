package com.cvesters.notula.meeting;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import com.cvesters.notula.common.controller.BaseController;
import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.meeting.bdo.MeetingDetails;
import com.cvesters.notula.meeting.dto.MeetingDetailsDto;

@Controller
public class MeetingWebSocket extends BaseController {

	private MeetingService meetingService;

	public MeetingWebSocket(final MeetingService meetingService) {
		this.meetingService = meetingService;
	}

	@SubscribeMapping("/meetings/{id}")
	public MeetingDetailsDto subscribe(@DestinationVariable final long id) {
		final Principal principal = getPrincipal();

		final MeetingDetails details = meetingService.getDetails(principal, id);
		return new MeetingDetailsDto(details);
	}

	@MessageMapping("/meetings/{id}")
	@SendTo("/topic/meetings/{id}")
	public String send(@DestinationVariable final long id,
			@Payload final String message) throws Exception {
		return "Echo: " + message;
		// return MessageBuilder.withPayload("Echo: " + message).build();
	}

}
