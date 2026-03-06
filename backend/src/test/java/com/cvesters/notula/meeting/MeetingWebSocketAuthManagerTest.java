package com.cvesters.notula.meeting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.messaging.access.intercept.MessageAuthorizationContext;

import com.cvesters.notula.common.domain.Principal;

class MeetingWebSocketAuthManagerTest {

	private static final Principal PRINCIPAL = mock();
	private static final long MEETING_ID = 4;

	private final MeetingService meetingService = mock();
	private final MeetingWebSocketAuthManager authManager = new MeetingWebSocketAuthManager(
			meetingService);

	@Nested
	class HasAccess {

		private final Message<String> message = MessageBuilder
				.withPayload("Hello")
				.build();

		@Test
		void access() {
			final var context = new MessageAuthorizationContext<>(message,
					Map.of("id", String.valueOf(MEETING_ID)));

			when(meetingService.existsById(PRINCIPAL, MEETING_ID))
					.thenReturn(true);

			assertThat(authManager.hasAccess(PRINCIPAL, context)).isTrue();
		}

		@Test
		void noAccess() {
			final var context = new MessageAuthorizationContext<>(message,
					Map.of("id", String.valueOf(MEETING_ID)));

			when(meetingService.existsById(PRINCIPAL, MEETING_ID))
					.thenReturn(false);

			assertThat(authManager.hasAccess(PRINCIPAL, context)).isFalse();
		}

		@Test
		void missingContextVariable() {
			final var context = new MessageAuthorizationContext<>(message);

			assertThat(authManager.hasAccess(PRINCIPAL, context)).isFalse();

		}
	}

	@Nested
	class GetPattern {

		@Test
		void success() {
			assertThat(authManager.getPattern())
					.isEqualTo("/*/meetings/{id:\\d+}");
		}
	}
}
