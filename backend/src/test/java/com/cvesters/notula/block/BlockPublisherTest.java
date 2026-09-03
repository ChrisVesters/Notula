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
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.block.bdo.BlockAction;
import com.cvesters.notula.block.bdo.BlockEvent;
import com.cvesters.notula.block.bdo.BlockInfo;
import com.cvesters.notula.block.dto.BlockEventDto;
import com.cvesters.notula.block.dto.BlockMutationDto;
import com.cvesters.notula.common.domain.Origin;
import com.cvesters.notula.common.dto.OriginDto;
import com.cvesters.notula.common.exception.MissingEntityException;
import com.cvesters.notula.common.messaging.TransactionalPublisher;
import com.cvesters.notula.meeting.TestMeeting;
import com.cvesters.notula.session.TestSession;
import com.cvesters.notula.topic.TestTopic;
import com.cvesters.notula.topic.TopicStorageGateway;
import com.cvesters.notula.topic.bdo.TopicInfo;

class BlockPublisherTest {

	private static final String DESTINATION_PREFIX = "/topic/meetings";

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
	private static final UUID CLIENT_ID = UUID
			.fromString("3f9c1a44-1d2e-4a51-8b0c-2c7e9b1d4a09");
	private static final Origin ORIGIN = new Origin(SESSION.principal(),
			CLIENT_ID);

	private final TransactionalPublisher publisher = mock();
	private final TopicStorageGateway topicStorage = mock();

	private final BlockPublisher blockPublisher = new BlockPublisher(
			publisher, topicStorage);

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
			final var event = new BlockEvent(block, action, ORIGIN);

			blockPublisher.publish(event);

			verify(publisher).send(eq(DESTINATION),
					argThat((BlockEventDto dto) -> {
						assertThat(dto.getBlockId()).isEqualTo(BLOCK_ID);
						assertThat(dto.getOrigin())
								.isEqualTo(new OriginDto(ORIGIN));

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
			final var event = new BlockEvent(block, action, ORIGIN);

			blockPublisher.publish(event);

			verify(publisher).send(eq(DESTINATION),
					argThat((BlockEventDto dto) -> {
						assertThat(dto.getBlockId()).isEqualTo(BLOCK_ID);
						assertThat(dto.getOrigin())
								.isEqualTo(new OriginDto(ORIGIN));

						assertThat(dto.getMutation())
								.isInstanceOf(BlockMutationDto.Delete.class);
						return true;
					}));
		}

		@Test
		void topicNotFound() {
			when(topicStorage.find(TOPIC_ID)).thenReturn(Optional.empty());

			final var action = new BlockAction.Delete();
			final var event = new BlockEvent(block, action, ORIGIN);

			assertThatThrownBy(() -> blockPublisher.publish(event))
					.isInstanceOf(MissingEntityException.class);

			verifyNoInteractions(publisher);
		}

		@Test
		void eventNull() {
			assertThatThrownBy(() -> blockPublisher.publish(null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
