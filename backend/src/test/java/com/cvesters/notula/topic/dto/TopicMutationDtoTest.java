package com.cvesters.notula.topic.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.topic.bdo.TopicAction;

class TopicMutationDtoTest {

	@Nested
	class Of {

		@Test
		void create() {
			final String name = "Intro";
			final var action = new TopicAction.Create(name);

			final var dto = TopicMutationDto.of(action);

			assertThat(dto).isInstanceOf(TopicMutationDto.Create.class);

			final var createDto = (TopicMutationDto.Create) dto;
			assertThat(createDto.getName()).isEqualTo(name);
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
		void actionNull() {
			assertThatThrownBy(() -> TopicMutationDto.of(null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
