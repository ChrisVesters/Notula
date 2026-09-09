package com.cvesters.notula.meeting.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.meeting.TestMeeting;
import com.cvesters.notula.meeting.bdo.MeetingInfo;

class MeetingInfoDtoTest {

	private static final TestMeeting MEETING = TestMeeting.SPORER_PROJECT;

	@Nested
	class Constructor {

		@Test
		void success() {
			final MeetingInfo bdo = MEETING.info();

			final var dto = new MeetingInfoDto(bdo);

			assertThat(dto.id()).isEqualTo(MEETING.getId());
			assertThat(dto.name()).isEqualTo(MEETING.getName());
			assertThat(dto.description()).isEqualTo(MEETING.getDescription());
		}

		@Test
		void withoutDescription() {
			final TestMeeting meeting = TestMeeting.SPORER_Q2_PLANNING;
			final MeetingInfo bdo = meeting.info();

			final var dto = new MeetingInfoDto(bdo);

			assertThat(dto.id()).isEqualTo(meeting.getId());
			assertThat(dto.name()).isEqualTo(meeting.getName());
			assertThat(dto.description()).isEmpty();
		}

		@Test
		void meetingNull() {
			assertThatThrownBy(() -> new MeetingInfoDto(null))
					.isInstanceOf(NullPointerException.class);
		}
	}

}
