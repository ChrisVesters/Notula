package com.cvesters.notula.meeting.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.meeting.TestMeeting;
import com.cvesters.notula.meeting.bdo.MeetingDetails;
import com.cvesters.notula.meeting.bdo.MeetingInfo;

class MeetingDetailsDtoTest {

	@Nested
	class Constructor {

		@Test
		void success() {
			final TestMeeting meeting = TestMeeting.GLOVER_KICKOFF_2026;
			final MeetingInfo info = meeting.info();
			final var details = new MeetingDetails(info);

			final var dto = new MeetingDetailsDto(details);

			assertThat(dto.info().id()).isEqualTo(info.getId());
			assertThat(dto.info().name()).isEqualTo(info.getName());
		}

		@Test
		void detailsNull() {
			final MeetingDetails details = null;

			assertThatThrownBy(() -> new MeetingDetailsDto(details))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
