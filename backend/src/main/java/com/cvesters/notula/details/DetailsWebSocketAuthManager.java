package com.cvesters.notula.details;

import java.util.Optional;

import org.springframework.security.messaging.access.intercept.MessageAuthorizationContext;
import org.springframework.stereotype.Component;

import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.config.WebSocketAuthManager;
import com.cvesters.notula.meeting.MeetingStorageGateway;

@Component
public class DetailsWebSocketAuthManager extends WebSocketAuthManager {

	private static final String MEETING_DESTINATION = "/topic/meetings/{id:\\d+}";

	private final MeetingStorageGateway meetingStorage;

	public DetailsWebSocketAuthManager(
			final MeetingStorageGateway meetingStorage) {
		super(MEETING_DESTINATION);

		this.meetingStorage = meetingStorage;
	}

	@Override
	public boolean hasAccess(final Principal principal,
			final MessageAuthorizationContext<?> context) {
		return Optional.ofNullable(context.getVariables().get("id"))
				.map(Long::parseLong)
				.flatMap(id -> meetingStorage.findByOrganisationIdAndId(
						principal.organisationId(), id))
				.isPresent();
	}

}
