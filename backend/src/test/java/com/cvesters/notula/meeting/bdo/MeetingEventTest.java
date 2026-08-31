package com.cvesters.notula.meeting.bdo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.common.domain.Origin;
import com.cvesters.notula.session.TestSession;

class MeetingEventTest {

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
	private static final UUID CLIENT_ID = UUID
			.fromString("3f9c1a44-1d2e-4a51-8b0c-2c7e9b1d4a03");
	private static final Origin ORIGIN = new Origin(SESSION.principal(),
			CLIENT_ID);

	@Nested
	class Constructor {

		@Test
		void success() {
			final var action = new MeetingAction.Create("New");
			final var event = new MeetingEvent(1L, action, ORIGIN);

			assertThat(event.meetingId()).isEqualTo(1L);
			assertThat(event.action()).isEqualTo(action);
			assertThat(event.origin()).isEqualTo(ORIGIN);
		}

		@Test
		void actionNull() {
			assertThatThrownBy(() -> new MeetingEvent(1L, null, ORIGIN))
					.isInstanceOf(NullPointerException.class);
		}

		@Test
		void originNull() {
			final var action = new MeetingAction.Create("New");

			assertThatThrownBy(() -> new MeetingEvent(1L, action, null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
