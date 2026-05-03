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

class TopicPublisherTest {

	private static final String DESTINATION_PREFIX = "/topic/meetings";

	private final SimpMessagingTemplate messagingTemplate = mock();
	private final TopicPublisher publisher = new TopicPublisher(
			messagingTemplate);

	@Nested
	class Publish {

		private static final long MEETING_ID = 1L;
		private static final TestTopic TOPIC = TestTopic.SPORER_PROJECT_BLOCKERS;

		private static final String DESTINATION = DESTINATION_PREFIX + "/"
				+ MEETING_ID;

		@Test
		void create() {
			final var action = new TopicAction.Create("New");
			final var event = new TopicEvent(TOPIC.getId(), action);

			publisher.publish(MEETING_ID, event);

			verify(messagingTemplate).convertAndSend(eq(DESTINATION),
					argThat((TopicEventDto dto) -> {
						assertThat(dto)
								.isInstanceOf(TopicEventDto.Create.class);

						final var createDto = (TopicEventDto.Create) dto;
						assertThat(createDto.getName()).isEqualTo("New");
						return true;
					}));
		}

		@Test
		void updateName() {
			// TODO
		}

		@Test
		void eventNull() {
			assertThatThrownBy(() -> publisher.publish(MEETING_ID, null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
