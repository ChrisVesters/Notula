package com.cvesters.notula.block;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.cvesters.notula.block.bdo.BlockAction;
import com.cvesters.notula.block.bdo.BlockEvent;
import com.cvesters.notula.block.dto.BlockEventDto;
import com.cvesters.notula.block.dto.BlockMutationDto;
import com.cvesters.notula.meeting.TestMeeting;
import com.cvesters.notula.topic.TestTopic;

class BlockPublisherTest {

	private static final String DESTINATION_PREFIX = "/topic/meetings";

	private final SimpMessagingTemplate messagingTemplate = mock();
	private final BlockPublisher publisher = new BlockPublisher(
			messagingTemplate);

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

		@Test
		void create() {
			final var action = new BlockAction.Create(BLOCK.getType(),
					BLOCK.getSequenceId());
			final var event = new BlockEvent(TOPIC_ID, BLOCK_ID, action);

			publisher.publish(MEETING_ID, event);

			verify(messagingTemplate).convertAndSend(eq(DESTINATION),
					argThat((BlockEventDto dto) -> {
						assertThat(dto.getTopicId()).isEqualTo(TOPIC_ID);
						assertThat(dto.getBlockId()).isEqualTo(BLOCK_ID);

						assertThat(dto.getMutation())
								.isInstanceOf(BlockMutationDto.Create.class);

						final var mutation = (BlockMutationDto.Create) dto
								.getMutation();
						assertThat(mutation.getType())
								.isEqualTo(BLOCK.getTypeString());
						assertThat(mutation.getSequenceId())
								.isEqualTo(BLOCK.getSequenceId());
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
