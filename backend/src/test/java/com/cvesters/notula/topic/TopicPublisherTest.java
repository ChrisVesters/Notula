package com.cvesters.notula.topic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.cvesters.notula.topic.bdo.TopicAction;
import com.cvesters.notula.topic.bdo.TopicEvent;
import com.cvesters.notula.topic.dto.TopicEventDto;
import com.cvesters.notula.topic.dto.TopicMutationDto;

class TopicPublisherTest {

	private static final String DESTINATION_PREFIX = "/topic/meetings";

	private final SimpMessagingTemplate messagingTemplate = mock();
	private final TopicPublisher publisher = new TopicPublisher(
			messagingTemplate);

	@Nested
	class Publish {

		private static final long MEETING_ID = 1L;
		private static final long TOPIC_ID = 32L;

		private static final String DESTINATION = DESTINATION_PREFIX + "/"
				+ MEETING_ID;

		@Test
		void create() {
			final var action = new TopicAction.Create("New");
			final var event = new TopicEvent(TOPIC_ID, action);

			publisher.publish(MEETING_ID, event);

			verify(messagingTemplate).convertAndSend(eq(DESTINATION),
					argThat((TopicEventDto dto) -> {
						assertThat(dto.getTopicId()).isEqualTo(TOPIC_ID);
						assertThat(dto.getMutation())
								.isInstanceOf(TopicMutationDto.Create.class);

						final var mutation = (TopicMutationDto.Create) dto
								.getMutation();
						assertThat(mutation.getName()).isEqualTo("New");
						return true;
					}));
		}

		@Test
		void updateName() {
			final var action = new TopicAction.UpdateName(4, 12, "Updated");
			final var event = new TopicEvent(TOPIC_ID, action);

			publisher.publish(MEETING_ID, event);

			verify(messagingTemplate).convertAndSend(eq(DESTINATION),
					argThat((TopicEventDto dto) -> {
						assertThat(dto.getTopicId()).isEqualTo(TOPIC_ID);
						assertThat(dto.getMutation()).isInstanceOf(
								TopicMutationDto.UpdateName.class);

						final var mutation = (TopicMutationDto.UpdateName) dto
								.getMutation();
						assertThat(mutation.getPosition()).isEqualTo(4);
						assertThat(mutation.getLength()).isEqualTo(12);
						assertThat(mutation.getValue()).isEqualTo("Updated");
						return true;
					}));
		}

		@Test
		void eventNull() {
			assertThatThrownBy(() -> publisher.publish(MEETING_ID, null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
