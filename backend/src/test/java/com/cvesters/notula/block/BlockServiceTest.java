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
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import com.cvesters.notula.block.bdo.BlockAction;
import com.cvesters.notula.block.bdo.BlockInfo;
import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.common.exception.MissingEntityException;
import com.cvesters.notula.meeting.TestMeeting;
import com.cvesters.notula.organisation.TestOrganisation;
import com.cvesters.notula.session.TestSession;
import com.cvesters.notula.topic.TestTopic;
import com.cvesters.notula.topic.TopicService;

class BlockServiceTest {

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
	private static final Principal PRINCIPAL = SESSION.principal();

	private final TopicService topicService = mock();

	private final BlockStorageGateway blockStorageGateway = mock();
	private final BlockPublisher blockPublisher = mock();

	private final BlockService blockService = new BlockService(topicService,
			blockStorageGateway, blockPublisher);

	@Nested
	class GetById {

		private static final TestBlock BLOCK = TestBlock.SPORER_PROJECT_BLOCKERS_FIRST;

		@Test
		void success() {
			final BlockInfo blockInfo = BLOCK.info();
			when(blockStorageGateway.find(BLOCK.getId()))
					.thenReturn(Optional.of(blockInfo));

			final BlockInfo result = blockService.getById(PRINCIPAL,
					BLOCK.getId());

			assertThat(result).isEqualTo(blockInfo);
		}

		@Test
		void notFound() {
			when(blockStorageGateway.find(BLOCK.getId()))
					.thenReturn(Optional.empty());

			final long blockId = BLOCK.getId();

			assertThatThrownBy(() -> blockService.getById(PRINCIPAL, blockId))
					.isInstanceOf(MissingEntityException.class);
		}

		@Test
		void otherOrganisation() {
			final Principal principal = TestSession.ALISON_DACH_GLOVER
					.principal();

			final BlockInfo blockInfo = BLOCK.info();
			when(blockStorageGateway.find(BLOCK.getId()))
					.thenReturn(Optional.of(blockInfo));

			final long blockId = BLOCK.getId();

			assertThatThrownBy(() -> blockService.getById(principal, blockId))
					.isInstanceOf(MissingEntityException.class);
		}

		@Test
		void principalNull() {
			final long blockId = BLOCK.getId();

			assertThatThrownBy(() -> blockService.getById(null, blockId))
					.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class Create {

		private static final TestBlock BLOCK = TestBlock.SPORER_PROJECT_BLOCKERS_FIRST;
		private static final TestTopic TOPIC = BLOCK.getTopic();
		private static final TestMeeting MEETING = TOPIC.getMeeting();
		private static final TestOrganisation ORGANISATION = MEETING
				.getOrganisation();

		@Test
		void firstBlock() {
			final long meetingId = MEETING.getId();
			final long topicId = TOPIC.getId();

			when(topicService.getById(PRINCIPAL, topicId))
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

			final var action = new BlockAction.Create(TOPIC.getId(),
					BLOCK.getType(), BLOCK.getSequenceId());

			final BlockInfo result = blockService.create(PRINCIPAL, meetingId,
					action);

			assertThat(result).isEqualTo(created);

			final var expectedAction = new BlockAction.Create(TOPIC.getId(),
					BLOCK.getType(), BLOCK.getSequenceId());
			final var matcher = new BlockActionMatcher.Create(expectedAction);
			verify(blockPublisher).publish(eq(meetingId), argThat(event -> {
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
		void blockAtEnd() {
			final long meetingId = MEETING.getId();
			final long topicId = TOPIC.getId();

			when(topicService.getById(PRINCIPAL, topicId))
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

			final var action = new BlockAction.Create(TOPIC.getId(),
					BLOCK.getType(), sequenceId);

			final BlockInfo result = blockService.create(PRINCIPAL, meetingId,
					action);

			assertThat(result).isEqualTo(created);

			final var expectedAction = new BlockAction.Create(TOPIC.getId(),
					BLOCK.getType(), sequenceId);
			final var matcher = new BlockActionMatcher.Create(expectedAction);
			verify(blockPublisher).publish(eq(meetingId), argThat(event -> {
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
		void blockAtStart() {
			final long meetingId = MEETING.getId();
			final long topicId = TOPIC.getId();

			when(topicService.getById(PRINCIPAL, topicId))
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

			final var action = new BlockAction.Create(TOPIC.getId(),
					BLOCK.getType(), sequenceId);

			final BlockInfo result = blockService.create(PRINCIPAL, meetingId,
					action);

			assertThat(result).isEqualTo(created);

			final var expectedAction = new BlockAction.Create(TOPIC.getId(),
					BLOCK.getType(), sequenceId);
			final var matcher = new BlockActionMatcher.Create(expectedAction);
			verify(blockPublisher).publish(eq(meetingId), argThat(event -> {
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

			when(topicService.getById(PRINCIPAL, topicId))
					.thenReturn(TOPIC.info());

			when(blockStorageGateway.findAllByTopicId(topicId))
					.thenReturn(Collections.emptyList());

			final var action = new BlockAction.Create(TOPIC.getId(),
					BLOCK.getType(), 1);

			assertThatThrownBy(
					() -> blockService.create(PRINCIPAL, meetingId, action))
							.isInstanceOf(IllegalArgumentException.class);

			verifyNoInteractions(blockPublisher);
			verify(blockStorageGateway, never()).updateAll(any());
			verify(blockStorageGateway, never()).create(any());
		}

		@Test
		void principalNull() {
			final long meetingId = MEETING.getId();

			final var block = new BlockAction.Create(TOPIC.getId(),
					BLOCK.getType(), BLOCK.getSequenceId());

			assertThatThrownBy(
					() -> blockService.create(null, meetingId, block))
							.isInstanceOf(NullPointerException.class);
		}

		@Test
		void actionNull() {
			final long meetingId = MEETING.getId();

			assertThatThrownBy(
					() -> blockService.create(PRINCIPAL, meetingId, null))
							.isInstanceOf(NullPointerException.class);
		}

	}

	@Nested
	class Delete {

		private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
		private static final TestBlock BLOCK = TestBlock.SPORER_PROJECT_BLOCKERS_FIRST;
		private static final TestTopic TOPIC = BLOCK.getTopic();
		private static final TestMeeting MEETING = TOPIC.getMeeting();

		@Test
		void onlyBlock() {
			final Principal principal = SESSION.principal();
			final long blockId = BLOCK.getId();
			final long topicId = TOPIC.getId();
			final long meetingId = MEETING.getId();

			final BlockInfo blockInfo = BLOCK.info();
			when(blockStorageGateway.find(blockId))
					.thenReturn(Optional.of(blockInfo));

			when(blockStorageGateway.findAllByTopicId(topicId))
					.thenReturn(List.of(blockInfo));

			blockService.delete(principal, meetingId, blockId);

			verify(blockStorageGateway).delete(blockInfo);
			verify(blockPublisher).publish(eq(meetingId), argThat(event -> {
				assertThat(event.blockId()).isEqualTo(blockId);
				assertThat(event.action())
						.isInstanceOf(BlockAction.Delete.class);
				return true;
			}));

			verify(blockStorageGateway).updateAll(Collections.emptyList());
		}

		@Test
		void firstBlock() {
			final TestTopic topic = TestTopic.SPORER_PROJECT_BLOCKERS;
			final TestMeeting meeting = topic.getMeeting();
			final List<TestBlock> blocks = TestBlock.ofTopic(topic);
			final TestBlock deleted = blocks.getFirst();

			final Principal principal = SESSION.principal();
			final long blockId = deleted.getId();
			final long topicId = topic.getId();
			final long meetingId = meeting.getId();

			final BlockInfo blockInfo = deleted.info();
			when(blockStorageGateway.find(blockId))
					.thenReturn(Optional.of(blockInfo));

			when(blockStorageGateway.findAllByTopicId(topicId))
					.thenReturn(List.of(blockInfo));
			final List<BlockInfo> existingBlocks = blocks.stream()
					.map(TestBlock::info)
					.toList();
			when(blockStorageGateway.findAllByTopicId(topicId))
					.thenReturn(existingBlocks);

			blockService.delete(principal, meetingId, blockId);

			verify(blockStorageGateway).delete(blockInfo);
			verify(blockPublisher).publish(eq(meetingId), argThat(event -> {
				assertThat(event.blockId()).isEqualTo(blockId);
				assertThat(event.action())
						.isInstanceOf(BlockAction.Delete.class);
				return true;
			}));

			// TODO: should have been moved up!
			final List<BlockInfo> toUpdateBlocks = existingBlocks.stream()
					.filter(t -> t.getId() != topicId)
					.toList();
			verify(blockStorageGateway).updateAll(toUpdateBlocks);
		}

		@Test
		void lastBlock() {
			final TestTopic topic = TestTopic.SPORER_PROJECT_BLOCKERS;
			final TestMeeting meeting = topic.getMeeting();
			final List<TestBlock> blocks = TestBlock.ofTopic(topic);
			final TestBlock deleted = blocks.getLast();

			final Principal principal = SESSION.principal();
			final long blockId = deleted.getId();
			final long topicId = topic.getId();
			final long meetingId = meeting.getId();

			final BlockInfo blockInfo = deleted.info();
			when(blockStorageGateway.find(blockId))
					.thenReturn(Optional.of(blockInfo));

			when(blockStorageGateway.findAllByTopicId(topicId))
					.thenReturn(List.of(blockInfo));
			final List<BlockInfo> existingBlocks = blocks.stream()
					.map(TestBlock::info)
					.toList();
			when(blockStorageGateway.findAllByTopicId(topicId))
					.thenReturn(existingBlocks);

			blockService.delete(principal, meetingId, blockId);

			verify(blockStorageGateway).delete(blockInfo);
			verify(blockPublisher).publish(eq(meetingId), argThat(event -> {
				assertThat(event.action())
						.isInstanceOf(BlockAction.Delete.class);
				return true;
			}));

			verify(blockStorageGateway).updateAll(Collections.emptyList());
		}

		@Test
		void notFound() {
			final TestTopic topic = TestTopic.SPORER_PROJECT_BLOCKERS;
			final TestMeeting meeting = topic.getMeeting();
			final List<TestBlock> blocks = TestBlock.ofTopic(topic);
			final TestBlock deleted = blocks.getLast();

			final Principal principal = SESSION.principal();
			final long blockId = deleted.getId();
			final long topicId = topic.getId();
			final long meetingId = meeting.getId();

			when(blockStorageGateway.find(blockId))
					.thenReturn(Optional.empty());

			assertThatThrownBy(
					() -> blockService.delete(principal, meetingId, blockId))
							.isInstanceOf(MissingEntityException.class);

			verify(blockStorageGateway, never()).delete(any());
			verifyNoInteractions(blockPublisher);
		}

		@Test
		void principalNull() {
			final long meetingId = MEETING.getId();
			final long blockId = BLOCK.getId();

			assertThatThrownBy(
					() -> blockService.delete(null, meetingId, blockId))
							.isInstanceOf(NullPointerException.class);
		}
	}

}
