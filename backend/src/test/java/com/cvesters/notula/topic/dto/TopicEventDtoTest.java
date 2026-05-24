package com.cvesters.notula.topic.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.topic.bdo.TopicAction;
import com.cvesters.notula.topic.bdo.TopicEvent;

class TopicEventDtoTest {

	@Nested
	class Constructor {

		private static final long TOPIC_ID = 32L;

		@Test
		void success() {
			final var action = new TopicAction.Create("New");
			final var event = new TopicEvent(TOPIC_ID, action);

			final var dto = new TopicEventDto(event);

			assertThat(dto.getTarget()).isEqualTo("TOPIC");
			assertThat(dto.getTopicId()).isEqualTo(TOPIC_ID);
			assertThat(dto.getMutation())
					.isInstanceOf(TopicMutationDto.Create.class);

			final var mutation = (TopicMutationDto.Create) dto.getMutation();
			assertThat(mutation.getName()).isEqualTo("New");
		}

		@Test
		void eventNull() {
			assertThatThrownBy(() -> new TopicEventDto(null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
