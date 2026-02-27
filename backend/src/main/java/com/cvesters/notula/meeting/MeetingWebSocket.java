package com.cvesters.notula.meeting;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import com.cvesters.notula.meeting.dto.MeetingDetailsDto;

@Controller
public class MeetingWebSocket {

	@MessageMapping("/meetings/{id}")
	@SendTo("/topic/meetings/{id}")
	public String send(@DestinationVariable final long id,
			@Payload final String message) throws Exception {
		return "Echo: " + message;
		// return MessageBuilder.withPayload("Echo: " + message).build();
	}

	// TODO: endpoint to return initial state on subscription.
	@SubscribeMapping("/meetings/{id}")
	public MeetingDetailsDto subscribe(@DestinationVariable final long id) {
		return new MeetingDetailsDto(id, "The Guardians Of The Galaxy");
	}
}
