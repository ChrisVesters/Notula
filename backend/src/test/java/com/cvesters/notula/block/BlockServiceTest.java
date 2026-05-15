package com.cvesters.notula.block;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import com.cvesters.notula.block.bdo.BlockAction;
import com.cvesters.notula.block.bdo.BlockInfo;
import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.meeting.TestMeeting;
import com.cvesters.notula.organisation.TestOrganisation;
import com.cvesters.notula.session.TestSession;
import com.cvesters.notula.topic.TestTopic;
import com.cvesters.notula.topic.TopicService;

class BlockServiceTest {

	private final TopicService topicService = mock();

	private final BlockStorageGateway blockStorageGateway = mock();
	private final BlockPublisher blockPublisher = mock();

	private final BlockService blockService = new BlockService(topicService,
			blockStorageGateway, blockPublisher);

	@Nested
	class Create {

		private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
		private static final Principal PRINCIPAL = SESSION.principal();

		private static final TestBlock BLOCK = TestBlock.SPORER_PROJECT_BLOCKERS_FIRST;
		private static final TestTopic TOPIC = BLOCK.getTopic();
		private static final TestMeeting MEETING = TOPIC.getMeeting();
		private static final TestOrganisation ORGANISATION = MEETING
				.getOrganisation();

		@Test
		void firstTopic() {
			final long meetingId = MEETING.getId();
			final long topicId = TOPIC.getId();

			when(topicService.getById(PRINCIPAL, meetingId, topicId))
					.thenReturn(TOPIC.info());

			when(blockStorageGateway.findAllByTopicId(topicId))
					.thenReturn(Collections.emptyList());

			final var created = new BlockInfo(BLOCK.getId(),
					ORGANISATION.getId(), TOPIC.getId(), BLOCK.getType(),
					BLOCK.getSequenceId());

			when(blockStorageGateway.create(argThat(t -> {
				assertThatThrownBy(t::getId)
						.isInstanceOf(IllegalStateException.class);
				assertThat(t.getOrganisationId())
						.isEqualTo(ORGANISATION.getId());
				assertThat(t.getTopicId()).isEqualTo(TOPIC.getId());
				assertThat(t.getType()).isEqualTo(BLOCK.getType());
				assertThat(t.getSequenceId()).isEqualTo(BLOCK.getSequenceId());
				return true;
			}))).thenReturn(created);

			final var action = new BlockAction.Create(BLOCK.getType(),
					BLOCK.getSequenceId());

			final BlockInfo result = blockService.create(PRINCIPAL, meetingId,
					topicId, action);

			assertThat(result).isEqualTo(created);

			final var expectedAction = new BlockAction.Create(BLOCK.getType(),
					BLOCK.getSequenceId());
			final var matcher = new BlockActionMatcher.Create(expectedAction);
			verify(blockPublisher).publish(eq(meetingId), argThat(event -> {
				assertThat(event.topicId()).isEqualTo(TOPIC.getId());
				assertThat(event.blockId()).isEqualTo(BLOCK.getId());
				assertThat(event.action()).is(matcher.equal());
				return true;
			}));

			final InOrder inOrder = inOrder(blockStorageGateway);
			inOrder.verify(blockStorageGateway)
					.updateAll(Collections.emptyList());
			inOrder.verify(blockStorageGateway).create(any());
		}

		@Test
		void existingTopicAtEnd() {
			final long meetingId = MEETING.getId();
			final long topicId = TOPIC.getId();

			when(topicService.getById(PRINCIPAL, meetingId, topicId))
					.thenReturn(TOPIC.info());

			final List<BlockInfo> existingBlocks = TestBlock.ofTopic(TOPIC)
					.stream()
					.map(TestBlock::info)
					.toList();
			when(blockStorageGateway.findAllByTopicId(topicId))
					.thenReturn(existingBlocks);

			final int sequenceId = existingBlocks.size();
			final var created = new BlockInfo(BLOCK.getId(),
					ORGANISATION.getId(), TOPIC.getId(), BLOCK.getType(),
					sequenceId);

			when(blockStorageGateway.create(argThat(t -> {
				assertThatThrownBy(t::getId)
						.isInstanceOf(IllegalStateException.class);
				assertThat(t.getOrganisationId())
						.isEqualTo(ORGANISATION.getId());
				assertThat(t.getTopicId()).isEqualTo(TOPIC.getId());
				assertThat(t.getType()).isEqualTo(BLOCK.getType());
				assertThat(t.getSequenceId()).isEqualTo(sequenceId);
				return true;
			}))).thenReturn(created);

			final var action = new BlockAction.Create(BLOCK.getType(),
					sequenceId);

			final BlockInfo result = blockService.create(PRINCIPAL, meetingId,
					topicId, action);

			assertThat(result).isEqualTo(created);

			final var expectedAction = new BlockAction.Create(BLOCK.getType(),
					sequenceId);
			final var matcher = new BlockActionMatcher.Create(expectedAction);
			verify(blockPublisher).publish(eq(meetingId), argThat(event -> {
				assertThat(event.topicId()).isEqualTo(TOPIC.getId());
				assertThat(event.blockId()).isEqualTo(BLOCK.getId());
				assertThat(event.action()).is(matcher.equal());
				return true;
			}));

			final InOrder inOrder = inOrder(blockStorageGateway);
			inOrder.verify(blockStorageGateway)
					.updateAll(Collections.emptyList());
			inOrder.verify(blockStorageGateway).create(any());
		}

		@Test
		void existingTopicAtStart() {
			final long meetingId = MEETING.getId();
			final long topicId = TOPIC.getId();

			when(topicService.getById(PRINCIPAL, meetingId, topicId))
					.thenReturn(TOPIC.info());

			final List<BlockInfo> existingBlocks = TestBlock.ofTopic(TOPIC)
					.stream()
					.map(TestBlock::info)
					.toList();
			when(blockStorageGateway.findAllByTopicId(topicId))
					.thenReturn(existingBlocks);

			final int sequenceId = 0;
			final var created = new BlockInfo(BLOCK.getId(),
					ORGANISATION.getId(), TOPIC.getId(), BLOCK.getType(),
					sequenceId);

			when(blockStorageGateway.create(argThat(t -> {
				assertThatThrownBy(t::getId)
						.isInstanceOf(IllegalStateException.class);
				assertThat(t.getOrganisationId())
						.isEqualTo(ORGANISATION.getId());
				assertThat(t.getTopicId()).isEqualTo(TOPIC.getId());
				assertThat(t.getType()).isEqualTo(BLOCK.getType());
				assertThat(t.getSequenceId()).isEqualTo(sequenceId);
				return true;
			}))).thenReturn(created);

			final var action = new BlockAction.Create(BLOCK.getType(),
					sequenceId);

			final BlockInfo result = blockService.create(PRINCIPAL, meetingId,
					topicId, action);

			assertThat(result).isEqualTo(created);

			final var expectedAction = new BlockAction.Create(BLOCK.getType(),
					sequenceId);
			final var matcher = new BlockActionMatcher.Create(expectedAction);
			verify(blockPublisher).publish(eq(meetingId), argThat(event -> {
				assertThat(event.topicId()).isEqualTo(TOPIC.getId());
				assertThat(event.blockId()).isEqualTo(BLOCK.getId());
				assertThat(event.action()).is(matcher.equal());
				return true;
			}));

			final InOrder inOrder = inOrder(blockStorageGateway);
			// TODO: should have been moved down!
			inOrder.verify(blockStorageGateway).updateAll(existingBlocks);
			inOrder.verify(blockStorageGateway).create(any());
		}

		@Test
		void invalidSequenceId() {
			final long meetingId = MEETING.getId();
			final long topicId = TOPIC.getId();

			when(topicService.getById(PRINCIPAL, meetingId, topicId))
					.thenReturn(TOPIC.info());

			when(blockStorageGateway.findAllByTopicId(topicId))
					.thenReturn(Collections.emptyList());

			final var action = new BlockAction.Create(BLOCK.getType(), 1);

			assertThatThrownBy(() -> blockService.create(PRINCIPAL, meetingId,
					topicId, action))
							.isInstanceOf(IllegalArgumentException.class);

			verifyNoInteractions(blockPublisher);
			verify(blockStorageGateway, never()).updateAll(any());
			verify(blockStorageGateway, never()).create(any());
		}

		@Test
		void principalNull() {
			final long meetingId = MEETING.getId();
			final long topicId = TOPIC.getId();

			final var block = new BlockAction.Create(BLOCK.getType(),
					BLOCK.getSequenceId());

			assertThatThrownBy(
					() -> blockService.create(null, meetingId, topicId, block))
							.isInstanceOf(NullPointerException.class);
		}

		@Test
		void actionNull() {
			final long meetingId = MEETING.getId();
			final long topicId = TOPIC.getId();

			assertThatThrownBy(() -> blockService.create(PRINCIPAL, meetingId,
					topicId, null)).isInstanceOf(NullPointerException.class);
		}

	}
}
