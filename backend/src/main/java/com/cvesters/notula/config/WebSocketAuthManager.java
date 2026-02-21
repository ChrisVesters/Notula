package com.cvesters.notula.config;

import java.util.function.Supplier;

import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.messaging.access.intercept.MessageAuthorizationContext;
import org.springframework.stereotype.Component;

import com.cvesters.notula.common.domain.Principal;

@Component
public class WebSocketAuthManager
		implements AuthorizationManager<MessageAuthorizationContext<?>> {

	// TODO: method to return list of protected topics
	public static final String MEETING_TOPC = "/topic/meetings";

	@Override
	public AuthorizationResult authorize(
			final Supplier<? extends Authentication> authentication,
			final MessageAuthorizationContext<?> context) {

		final StompHeaderAccessor accessor = MessageHeaderAccessor
				.getAccessor(context.getMessage(), StompHeaderAccessor.class);
		final String destination = accessor
				.getFirstNativeHeader(StompHeaders.DESTINATION);
		if (destination == null || !destination.startsWith(MEETING_TOPC)) {
			return new AuthorizationDecision(false);
		}

		final long meetingId = Long
				.parseLong(destination.substring(MEETING_TOPC.length() + 1));

		final Authentication auth = authentication.get();

		if (auth.getPrincipal() instanceof Principal principal) {
			// TODO: see if user actually has access to meeting.
			return new AuthorizationDecision(true);
		}

		return new AuthorizationDecision(false);
	}

}