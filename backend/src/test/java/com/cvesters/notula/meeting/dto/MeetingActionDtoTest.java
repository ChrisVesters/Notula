package com.cvesters.notula.meeting.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.meeting.TestMeeting;
import com.cvesters.notula.meeting.bdo.MeetingAction;

class MeetingActionDtoTest {

	private static final TestMeeting MEETING = TestMeeting.SPORER_RETRO;

	@Nested
	class Create {

		@Test
		void toBdo() {
			final var dto = new MeetingActionDto.Create(MEETING.getName());
			final MeetingAction.Create bdo = dto.toBdo();

			assertThat(bdo).isEqualTo(MEETING.create());
		}
	}

}
