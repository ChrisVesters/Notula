package com.cvesters.notula.topic.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.topic.TestTopic;
import com.cvesters.notula.topic.bdo.TopicEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

class TopicEventDtoTest {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	@Nested
	class Create {

		private static final TestTopic TOPIC = TestTopic.SPORER_PROJECT_BLOCKERS;
		private static final TopicEvent.Create bdo = TOPIC.createEvent();

		@Test
		void of() {
			final var dto = TopicEventDto.of(bdo);

			assertThat(dto).isInstanceOfSatisfying(TopicEventDto.Create.class,
					create -> {
						assertThat(create.id()).isEqualTo(TOPIC.getId());
						assertThat(create.name()).isEqualTo(TOPIC.getName());
					});
		}

		@Test
		void ofBdo() {
			final var dto = new TopicEventDto.Create(bdo);

			assertThat(dto.id()).isEqualTo(TOPIC.getId());
			assertThat(dto.name()).isEqualTo(TOPIC.getName());
		}

		@Test
		void ofBdoNull() {
			assertThatThrownBy(() -> new TopicEventDto.Create(null))
					.isInstanceOf(NullPointerException.class);
		}

		@Test
		void jackson() throws JsonProcessingException {
			final var dto = TopicEventDto.of(bdo);

			final String json = MAPPER.writeValueAsString(dto);

			assertThat(json).isEqualToIgnoringWhitespace("""
				{
					"type": "TOPIC_CREATE",
					"id": %d,
					"name": "%s"
				}
			""".formatted(TOPIC.getId(), TOPIC.getName()));
		}
	}
}
