package com.cvesters.notula.topic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.cvesters.notula.topic.bdo.TopicAction;
import com.cvesters.notula.topic.bdo.TopicEvent;
import com.cvesters.notula.topic.bdo.TopicInfo;
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

		private final TopicInfo topic = mock();

		@BeforeEach
		void topic() {
			when(topic.getId()).thenReturn(TOPIC_ID);
			when(topic.getMeetingId()).thenReturn(MEETING_ID);
		}

		@Test
		void create() {
			final var action = new TopicAction.Create(MEETING_ID, 3, "New");
			final var event = new TopicEvent(topic, action);

			publisher.publish(event);

			verify(messagingTemplate).convertAndSend(eq(DESTINATION),
					argThat((TopicEventDto dto) -> {
						assertThat(dto.getTopicId()).isEqualTo(TOPIC_ID);
						assertThat(dto.getMutation())
								.isInstanceOf(TopicMutationDto.Create.class);

						final var mutation = (TopicMutationDto.Create) dto
								.getMutation();
						assertThat(mutation.getSequenceId()).isEqualTo(3);
						assertThat(mutation.getName()).isEqualTo("New");
						return true;
					}));
		}

		@Test
		void updateName() {
			final var action = new TopicAction.UpdateName(4, 12, "Updated");
			final var event = new TopicEvent(topic, action);

			publisher.publish(event);

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
		void updateDescription() {
			final var action = new TopicAction.UpdateDescription(4, 12,
					"Updated");
			final var event = new TopicEvent(topic, action);

			publisher.publish(event);

			verify(messagingTemplate).convertAndSend(eq(DESTINATION),
					argThat((TopicEventDto dto) -> {
						assertThat(dto.getTopicId()).isEqualTo(TOPIC_ID);
						assertThat(dto.getMutation()).isInstanceOf(
								TopicMutationDto.UpdateDescription.class);

						final var mutation = (TopicMutationDto.UpdateDescription) dto
								.getMutation();
						assertThat(mutation.getPosition()).isEqualTo(4);
						assertThat(mutation.getLength()).isEqualTo(12);
						assertThat(mutation.getValue()).isEqualTo("Updated");
						return true;
					}));
		}

		@Test
		void delete() {
			final var action = new TopicAction.Delete();
			final var event = new TopicEvent(topic, action);

			publisher.publish(event);

			verify(messagingTemplate).convertAndSend(eq(DESTINATION),
					argThat((TopicEventDto dto) -> {
						assertThat(dto.getTopicId()).isEqualTo(TOPIC_ID);
						assertThat(dto.getMutation())
								.isInstanceOf(TopicMutationDto.Delete.class);
						return true;
					}));

		}

		@Test
		void eventNull() {
			assertThatThrownBy(() -> publisher.publish(null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
