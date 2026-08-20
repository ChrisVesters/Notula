package com.cvesters.notula.topic.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.topic.bdo.TopicAction;
import com.cvesters.notula.topic.bdo.TopicEvent;
import com.cvesters.notula.topic.bdo.TopicInfo;

class TopicEventDtoTest {

	@Nested
	class Constructor {

		private static final long MEETING_ID = 1L;
		private static final long TOPIC_ID = 32L;

		@Test
		void success() {
			final TopicInfo topic = mock();
			when(topic.getId()).thenReturn(TOPIC_ID);

			final var action = new TopicAction.Create(MEETING_ID, 3, "New");
			final var event = new TopicEvent(topic, action);

			final var dto = new TopicEventDto(event);

			assertThat(dto.getTarget()).isEqualTo("TOPIC");
			assertThat(dto.getTopicId()).isEqualTo(TOPIC_ID);
			assertThat(dto.getMutation())
					.isInstanceOf(TopicMutationDto.Create.class);

			final var mutation = (TopicMutationDto.Create) dto.getMutation();
			assertThat(mutation.getMeetingId()).isEqualTo(MEETING_ID);
			assertThat(mutation.getSequenceId()).isEqualTo(3);
			assertThat(mutation.getName()).isEqualTo("New");
		}

		@Test
		void eventNull() {
			assertThatThrownBy(() -> new TopicEventDto(null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
