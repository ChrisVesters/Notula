package com.cvesters.notula.meeting.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.meeting.bdo.MeetingAction;

class MeetingMutationDtoTest {

	@Nested
	class Of {

		@Test
		void create() {
			final String name = "Q4 Planning";
			final var action = new MeetingAction.Create(name);

			final var dto = MeetingMutationDto.of(action);

			assertThat(dto).isInstanceOf(MeetingMutationDto.Create.class);

			final var createDto = (MeetingMutationDto.Create) dto;
			assertThat(createDto.getName()).isEqualTo(name);
		}

		@Test
		void updateName() {
			final int position = 4;
			final int length = 12;
			final String value = "Updated";
			final var action = new MeetingAction.UpdateName(position, length,
					value);

			final var dto = MeetingMutationDto.of(action);

			assertThat(dto).isInstanceOf(MeetingMutationDto.UpdateName.class);

			final var updateNameDto = (MeetingMutationDto.UpdateName) dto;
			assertThat(updateNameDto.getPosition()).isEqualTo(position);
			assertThat(updateNameDto.getLength()).isEqualTo(length);
			assertThat(updateNameDto.getValue()).isEqualTo(value);
		}

		@Test
		void updateDescription() {
			final int position = 4;
			final int length = 12;
			final String value = "Updated";
			final var action = new MeetingAction.UpdateDescription(position,
					length, value);

			final var dto = MeetingMutationDto.of(action);

			assertThat(dto)
					.isInstanceOf(MeetingMutationDto.UpdateDescription.class);

			final var updateDescriptionDto = (MeetingMutationDto.UpdateDescription) dto;
			assertThat(updateDescriptionDto.getPosition()).isEqualTo(position);
			assertThat(updateDescriptionDto.getLength()).isEqualTo(length);
			assertThat(updateDescriptionDto.getValue()).isEqualTo(value);
		}

		@Test
		void actionNull() {
			assertThatThrownBy(() -> MeetingMutationDto.of(null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
