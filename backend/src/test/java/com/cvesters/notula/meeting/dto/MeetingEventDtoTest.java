package com.cvesters.notula.meeting.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.meeting.bdo.MeetingAction;
import com.cvesters.notula.meeting.bdo.MeetingEvent;

class MeetingEventDtoTest {

	@Nested
	class Constructor {

		private static final long MEETING_ID = 18L;

		@Test
		void success() {
			final var action = new MeetingAction.Create("New");
			final var event = new MeetingEvent(MEETING_ID, action);

			final var dto = new MeetingEventDto(event);

			assertThat(dto.getTarget()).isEqualTo("MEETING");
			assertThat(dto.getMeetingId()).isEqualTo(MEETING_ID);
			assertThat(dto.getMutation())
					.isInstanceOf(MeetingMutationDto.Create.class);

			final var mutation = (MeetingMutationDto.Create) dto.getMutation();
			assertThat(mutation.getName()).isEqualTo("New");
		}

		@Test
		void eventNull() {
			assertThatThrownBy(() -> new MeetingEventDto(null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
