package com.cvesters.notula.topic.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.common.domain.Minutes;
import com.cvesters.notula.topic.bdo.TopicAction;

class TopicMutationDtoTest {

	private static final long MEETING_ID = 1L;

	@Nested
	class Of {

		@Test
		void create() {
			final String name = "Intro";
			final var action = new TopicAction.Create(MEETING_ID, 0, name);

			final var dto = TopicMutationDto.of(action);

			assertThat(dto).isInstanceOf(TopicMutationDto.Create.class);

			final var createDto = (TopicMutationDto.Create) dto;
			assertThat(createDto.getMeetingId()).isEqualTo(MEETING_ID);
			assertThat(createDto.getSequenceId()).isZero();
			assertThat(createDto.getName()).isEqualTo(name);
		}

		@Test
		void move() {
			final int sequenceId = 2;
			final var action = new TopicAction.Move(sequenceId);

			final var dto = TopicMutationDto.of(action);

			assertThat(dto).isInstanceOf(TopicMutationDto.Move.class);

			final var moveDto = (TopicMutationDto.Move) dto;
			assertThat(moveDto.getSequenceId()).isEqualTo(sequenceId);
		}

		@Test
		void updateName() {
			final int position = 4;
			final int length = 12;
			final String value = "Updated";
			final var action = new TopicAction.UpdateName(position, length,
					value);

			final var dto = TopicMutationDto.of(action);

			assertThat(dto).isInstanceOf(TopicMutationDto.UpdateName.class);

			final var updateNameDto = (TopicMutationDto.UpdateName) dto;
			assertThat(updateNameDto.getPosition()).isEqualTo(position);
			assertThat(updateNameDto.getLength()).isEqualTo(length);
			assertThat(updateNameDto.getValue()).isEqualTo(value);
		}

		@Test
		void updateDescription() {
			final int position = 4;
			final int length = 12;
			final String value = "Updated";
			final var action = new TopicAction.UpdateDescription(position,
					length, value);

			final var dto = TopicMutationDto.of(action);

			assertThat(dto)
					.isInstanceOf(TopicMutationDto.UpdateDescription.class);

			final var updateDescriptionDto = (TopicMutationDto.UpdateDescription) dto;
			assertThat(updateDescriptionDto.getPosition()).isEqualTo(position);
			assertThat(updateDescriptionDto.getLength()).isEqualTo(length);
			assertThat(updateDescriptionDto.getValue()).isEqualTo(value);
		}

		@Test
		void updateDuration() {
			final var duration = new Minutes(45);
			final var action = new TopicAction.UpdateDuration(duration);

			final var dto = TopicMutationDto.of(action);

			assertThat(dto).isInstanceOf(TopicMutationDto.UpdateDuration.class);

			final var updateDurationDto = (TopicMutationDto.UpdateDuration) dto;
			assertThat(updateDurationDto.getDuration()).isEqualTo(45);
		}

		@Test
		void updateDurationNull() {
			final var action = new TopicAction.UpdateDuration(null);

			final var dto = TopicMutationDto.of(action);

			assertThat(dto).isInstanceOf(TopicMutationDto.UpdateDuration.class);

			final var updateDurationDto = (TopicMutationDto.UpdateDuration) dto;
			assertThat(updateDurationDto.getDuration()).isNull();
		}

		@Test
		void delete() {
			final var action = new TopicAction.Delete();

			final var dto = TopicMutationDto.of(action);

			assertThat(dto).isInstanceOf(TopicMutationDto.Delete.class);
		}	

		@Test
		void actionNull() {
			assertThatThrownBy(() -> TopicMutationDto.of(null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
