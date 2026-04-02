package com.cvesters.notula.meeting;

import java.util.Optional;

import org.springframework.security.messaging.access.intercept.MessageAuthorizationContext;
import org.springframework.stereotype.Component;

import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.config.WebSocketAuthManager;

@Component
public class MeetingWebSocketAuthManager extends WebSocketAuthManager {

	private static final String MEETING_DESTINATION = "/topic/meetings/{id:\\d+}";

	private final MeetingService meetingService;

	public MeetingWebSocketAuthManager(final MeetingService meetingService) {
		super(MEETING_DESTINATION);

		this.meetingService = meetingService;
	}

	@Override
	public boolean hasAccess(final Principal principal,
			final MessageAuthorizationContext<?> context) {
		return Optional.ofNullable(context.getVariables().get("id"))
				.map(Long::parseLong)
				.map(id -> meetingService.existsById(principal, id))
				.orElse(false);
	}

}
