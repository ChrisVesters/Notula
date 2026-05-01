package com.cvesters.notula.topic.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.cvesters.notula.topic.bdo.TopicAction;
import com.cvesters.notula.topic.bdo.TopicEvent;

class TopicEventDtoTest {

	@Test
	void eventNull() {
		assertThatThrownBy(() -> TopicEventDto.of(null))
				.isExactlyInstanceOf(NullPointerException.class);
	}

	@Test
	void create() {
		final var action = new TopicAction.Create("New");
		final var event = new TopicEvent(1L, action);

		final var dto = TopicEventDto.of(event);
		assertThat(dto).isExactlyInstanceOf(TopicEventDto.Create.class);
		assertThat(dto.getTopicId()).isEqualTo(1L);

		final var createDto = (TopicEventDto.Create) dto;
		assertThat(createDto.getName()).isEqualTo("New");
	}

	@Test
	void updateName() {
		final var action = new TopicAction.UpdateName(2, 4, "27");
		final var event = new TopicEvent(1L, action);

		final var dto = TopicEventDto.of(event);
		assertThat(dto).isExactlyInstanceOf(TopicEventDto.UpdateName.class);
		assertThat(dto.getTopicId()).isEqualTo(1L);

		final var updateDto = (TopicEventDto.UpdateName) dto;
		assertThat(updateDto.getPosition()).isEqualTo(2);
		assertThat(updateDto.getLength()).isEqualTo(4);
		assertThat(updateDto.getValue()).isEqualTo("27");
	}
}
