package com.cvesters.notula.meeting.bdo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.meeting.TestMeeting;

class MeetingDetailsTest {

	@Nested
	class Constructor {

		@Test
		void success() {
			final TestMeeting meeting = TestMeeting.GLOVER_KICKOFF_2026;
			final MeetingInfo info = meeting.info();

			final var details = new MeetingDetails(info);

			assertThat(details.info()).isEqualTo(info);
		}

		@Test
		void infoNull() {
			assertThatThrownBy(() -> new MeetingDetails(null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
