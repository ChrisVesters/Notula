package com.cvesters.notula.block;

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

import com.cvesters.notula.block.bdo.BlockAction;
import com.cvesters.notula.block.bdo.BlockEvent;
import com.cvesters.notula.block.bdo.BlockInfo;
import com.cvesters.notula.block.dto.BlockEventDto;
import com.cvesters.notula.block.dto.BlockMutationDto;
import com.cvesters.notula.common.exception.MissingEntityException;
import com.cvesters.notula.meeting.TestMeeting;
import com.cvesters.notula.topic.TestTopic;
import com.cvesters.notula.topic.TopicStorageGateway;
import com.cvesters.notula.topic.bdo.TopicInfo;

class BlockPublisherTest {

	private static final String DESTINATION_PREFIX = "/topic/meetings";

	private final SimpMessagingTemplate messagingTemplate = mock();
	private final TopicStorageGateway topicStorage = mock();

	private final BlockPublisher publisher = new BlockPublisher(
			messagingTemplate, topicStorage);

	@Nested
	class Publish {

		private static final TestBlock BLOCK = TestBlock.SPORER_PROJECT_BLOCKERS_FIRST;
		private static final TestTopic TOPIC = BLOCK.getTopic();
		private static final TestMeeting MEETING = TOPIC.getMeeting();

		private static final long MEETING_ID = MEETING.getId();
		private static final long TOPIC_ID = TOPIC.getId();
		private static final long BLOCK_ID = BLOCK.getId();

		private static final String DESTINATION = DESTINATION_PREFIX + "/"
				+ MEETING_ID;

		private final BlockInfo block = mock();

		@BeforeEach
		void block() {
			when(block.getId()).thenReturn(BLOCK_ID);
			when(block.getTopicId()).thenReturn(TOPIC_ID);
		}

		@Test
		void create() {
			final TopicInfo topic = mock();
			when(topic.getMeetingId()).thenReturn(MEETING_ID);
			when(topicStorage.find(TOPIC_ID)).thenReturn(Optional.of(topic));

			final var action = new BlockAction.Create(TOPIC_ID, BLOCK.getType(),
					BLOCK.getSequenceId());
			final var event = new BlockEvent(block, action);

			publisher.publish(event);

			verify(messagingTemplate).convertAndSend(eq(DESTINATION),
					argThat((BlockEventDto dto) -> {
						assertThat(dto.getBlockId()).isEqualTo(BLOCK_ID);

						assertThat(dto.getMutation())
								.isInstanceOf(BlockMutationDto.Create.class);

						final var mutation = (BlockMutationDto.Create) dto
								.getMutation();
						assertThat(mutation.getTopicId()).isEqualTo(TOPIC_ID);
						assertThat(mutation.getType())
								.isEqualTo(BLOCK.getTypeDto());
						assertThat(mutation.getSequenceId())
								.isEqualTo(BLOCK.getSequenceId());
						return true;
					}));
		}

		@Test
		void delete() {
			final TopicInfo topic = mock();
			when(topic.getMeetingId()).thenReturn(MEETING_ID);
			when(topicStorage.find(TOPIC_ID)).thenReturn(Optional.of(topic));

			final var action = new BlockAction.Delete();
			final var event = new BlockEvent(block, action);

			publisher.publish(event);

			verify(messagingTemplate).convertAndSend(eq(DESTINATION),
					argThat((BlockEventDto dto) -> {
						assertThat(dto.getBlockId()).isEqualTo(BLOCK_ID);
						assertThat(dto.getMutation())
								.isInstanceOf(BlockMutationDto.Delete.class);
						return true;
					}));
		}

		@Test
		void topicNotFound() {
			when(topicStorage.find(TOPIC_ID)).thenReturn(Optional.empty());

			final var action = new BlockAction.Delete();
			final var event = new BlockEvent(block, action);

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
