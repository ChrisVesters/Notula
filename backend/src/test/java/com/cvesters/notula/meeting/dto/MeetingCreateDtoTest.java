package com.cvesters.notula.meeting.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.cvesters.notula.meeting.TestMeeting;

class MeetingCreateDtoTest {

	private static final TestMeeting MEETING = TestMeeting.SPORER_RETRO;

	@Test
	void toBdo() {
		final var dto = new MeetingCreateDto(MEETING.getName());
		final var bdo = dto.toBdo(MEETING.getOrganisation().getId());

		assertThatThrownBy(() -> bdo.getId())
				.isInstanceOf(IllegalStateException.class);
		assertThat(bdo.getOrganisationId())
				.isEqualTo(MEETING.getOrganisation().getId());
		assertThat(bdo.getName()).isEqualTo(MEETING.getName());
	}
}
