package com.cvesters.notula.meeting.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.cvesters.notula.meeting.bdo.MeetingAction;
import com.cvesters.notula.meeting.bdo.MeetingEvent;

class MeetingEventDtoTest {

	@Test
	void eventNull() {
		assertThatThrownBy(() -> MeetingEventDto.of(null))
				.isExactlyInstanceOf(NullPointerException.class);
	}

	@Test
	void create() {
		final var action = new MeetingAction.Create("New");
		final var event = new MeetingEvent(1L, action);

		final var dto = MeetingEventDto.of(event);
		assertThat(dto).isExactlyInstanceOf(MeetingEventDto.Create.class);
		assertThat(dto.getMeetingId()).isEqualTo(1L);

		final var createDto = (MeetingEventDto.Create) dto;
		assertThat(createDto.getName()).isEqualTo("New");
	}

	@Test
	void updateName() {
		final var action = new MeetingAction.UpdateName(2, 4, "27");
		final var event = new MeetingEvent(1L, action);

		final var dto = MeetingEventDto.of(event);
		assertThat(dto).isExactlyInstanceOf(MeetingEventDto.UpdateName.class);
		assertThat(dto.getMeetingId()).isEqualTo(1L);

		final var updateDto = (MeetingEventDto.UpdateName) dto;
		assertThat(updateDto.getPosition()).isEqualTo(2);
		assertThat(updateDto.getLength()).isEqualTo(4);
		assertThat(updateDto.getValue()).isEqualTo("27");
	}
}
