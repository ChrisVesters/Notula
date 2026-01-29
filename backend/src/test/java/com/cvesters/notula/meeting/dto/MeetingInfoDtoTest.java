package com.cvesters.notula.meeting.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.meeting.TestMeeting;

class MeetingInfoDtoTest {

	private static final TestMeeting MEETING = TestMeeting.SPORER_Q2_PLANNING;

	@Nested
	class Constructor {

		@Test
		void success() {
			final var bdo = MEETING.info();

			final var dto = new MeetingInfoDto(bdo);

			assertThat(dto.id()).isEqualTo(MEETING.getId());
			assertThat(dto.name()).isEqualTo(MEETING.getName());
		}

		@Test
		void meetingNull() {
			assertThatThrownBy(() -> new MeetingInfoDto(null))
					.isInstanceOf(NullPointerException.class);
		}
	}

}
