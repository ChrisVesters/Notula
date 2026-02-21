package com.cvesters.notula.meeting;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

@Controller
public class MeetingWebSocket {

	@MessageMapping("/meetings/{id}")
	@SendTo("/topic/meetings/{id}")
	public String send(@DestinationVariable final long id,
			@Payload final String message) throws Exception {
				// TODO: let authentication/security be handled by the AuthManager.
		final Authentication authentication = SecurityContextHolder.getContext()
				.getAuthentication();
		return "Echo: " + message;
		// return MessageBuilder.withPayload("Echo: " + message).build();
	}

	// TODO: endpoint to return initial state on subscription.
}
