package com.cvesters.notula.meeting;

import org.springframework.security.messaging.access.intercept.MessageAuthorizationContext;
import org.springframework.stereotype.Component;

import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.config.WebSocketAuthManager;

@Component
public class MeetingWebSocketAuthManager extends WebSocketAuthManager {

	private static final String MEETING_TOPC = "/*/meetings/{id:\\d+}";

	private final MeetingService meetingService;

	public MeetingWebSocketAuthManager(final MeetingService meetingService) {
		super(MEETING_TOPC);

		this.meetingService = meetingService;
	}

	@Override
	public boolean hasAccess(final Principal principal,
			final MessageAuthorizationContext<?> context) {
		final long meetingId = Long.parseLong(context.getVariables().get("id"));

		return meetingService.existsById(principal, meetingId);
	}

}
