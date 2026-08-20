package com.cvesters.notula.textblock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.cvesters.notula.block.bdo.BlockInfo;
import com.cvesters.notula.common.exception.MissingEntityException;
import com.cvesters.notula.textblock.bdo.TextBlockAction;
import com.cvesters.notula.textblock.dao.TextBlockEvent;
import com.cvesters.notula.textblock.dto.TextBlockEventDto;
import com.cvesters.notula.textblock.dto.TextBlockMutationDto;
import com.cvesters.notula.topic.TopicStorageGateway;
import com.cvesters.notula.topic.bdo.TopicInfo;

class TextBlockPublisherTest {

	private static final String DESTINATION_PREFIX = "/topic/meetings";

	private final SimpMessagingTemplate messagingTemplate = mock();
	private final TopicStorageGateway topicStorage = mock();
	private final TextBlockPublisher publisher = new TextBlockPublisher(
			messagingTemplate, topicStorage);

	@Nested
	class Publish {

		private static final long MEETING_ID = 1L;
		private static final long TOPIC_ID = 32L;
		private static final long BLOCK_ID = 61L;

		private static final String DESTINATION = DESTINATION_PREFIX + "/"
				+ MEETING_ID;

		private final BlockInfo block = mock();

		@BeforeEach
		void block() {
			when(block.getId()).thenReturn(BLOCK_ID);
			when(block.getTopicId()).thenReturn(TOPIC_ID);
		}

		@Test
		void updateContent() {
			final TopicInfo topic = mock();
			when(topic.getMeetingId()).thenReturn(MEETING_ID);
			when(topicStorage.find(TOPIC_ID)).thenReturn(Optional.of(topic));

			final var action = new TextBlockAction.UpdateContent(2, 3, "New");
			final var event = new TextBlockEvent(block, action);

			publisher.publish(event);

			verify(messagingTemplate).convertAndSend(eq(DESTINATION),
					argThat((TextBlockEventDto dto) -> {
						assertThat(dto.getBlockId()).isEqualTo(BLOCK_ID);
						assertThat(dto.getMutation()).isInstanceOf(
								TextBlockMutationDto.UpdateContent.class);

						final var mutation = (TextBlockMutationDto.UpdateContent) dto
								.getMutation();
						assertThat(mutation.getPosition()).isEqualTo(2);
						assertThat(mutation.getLength()).isEqualTo(3);
						assertThat(mutation.getValue()).isEqualTo("New");
						return true;
					}));
		}

		@Test
		void topicNotFound() {
			final TopicInfo topic = mock();
			when(topic.getMeetingId()).thenReturn(MEETING_ID);
			when(topicStorage.find(TOPIC_ID)).thenReturn(Optional.of(topic));

			when(topicStorage.find(TOPIC_ID)).thenReturn(Optional.empty());

			final var action = new TextBlockAction.UpdateContent(2, 3, "New");
			final var event = new TextBlockEvent(block, action);

			assertThatThrownBy(() -> publisher.publish(event))
					.isInstanceOf(MissingEntityException.class);

			verifyNoInteractions(messagingTemplate);
		}

		@Test
		void eventNull() {
			assertThatThrownBy(() -> publisher.publish(null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
