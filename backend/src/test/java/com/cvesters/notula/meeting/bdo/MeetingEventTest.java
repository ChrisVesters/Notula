package com.cvesters.notula.meeting.bdo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MeetingEventTest {

	@Nested
	class Constructor {

		@Test
		void success() {
			final var action = new MeetingAction.Create("New");
			final var event = new MeetingEvent(1L, action);

			assertThat(event.meetingId()).isEqualTo(1L);
			assertThat(event.action()).isEqualTo(action);
		}

		@Test
		void actionNull() {
			assertThatThrownBy(() -> new MeetingEvent(1L, null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
