package com.cvesters.notula.block;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import com.cvesters.notula.block.bdo.BlockAction;
import com.cvesters.notula.block.bdo.BlockEvent;
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

			final BlockInfo result = blockService.create(PRINCIPAL, action);

			assertThat(result).isEqualTo(created);

			final var expectedAction = new BlockAction.Create(TOPIC.getId(),
					BLOCK.getType(), BLOCK.getSequenceId());
			final var matcher = new BlockActionMatcher.Create(expectedAction);
			verify(blockPublisher).publish(argThat(event -> {
				assertThat(event.block()).isEqualTo(created);
				assertThat(event.action()).is(matcher.equal());
				return true;
			}));

			final InOrder inOrder = inOrder(blockStorageGateway);
			verify(blockStorageGateway, never()).update(any());
			inOrder.verify(blockStorageGateway).create(any());
		}

		@Test
		void blockAtEnd() {
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

			final BlockInfo result = blockService.create(PRINCIPAL, action);

			assertThat(result).isEqualTo(created);

			final var expectedAction = new BlockAction.Create(TOPIC.getId(),
					BLOCK.getType(), sequenceId);
			final var matcher = new BlockActionMatcher.Create(expectedAction);
			verify(blockPublisher).publish(argThat(event -> {
				assertThat(event.block()).isEqualTo(created);
				assertThat(event.action()).is(matcher.equal());
				return true;
			}));

			verify(blockStorageGateway, never()).update(any());
			verify(blockStorageGateway).create(any());
		}

		@Test
		void blockAtStart() {
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

			final BlockInfo result = blockService.create(PRINCIPAL, action);

			assertThat(result).isEqualTo(created);

			final var expectedAction = new BlockAction.Create(TOPIC.getId(),
					BLOCK.getType(), sequenceId);
			final var matcher = new BlockActionMatcher.Create(expectedAction);
			verify(blockPublisher).publish(argThat(event -> {
				assertThat(event.block()).isEqualTo(created);
				assertThat(event.action()).is(matcher.equal());
				return true;
			}));

			final InOrder inOrder = inOrder(blockStorageGateway);
			// TODO: should have been moved down!
			existingBlocks.forEach(
					b -> inOrder.verify(blockStorageGateway).update(b));
			inOrder.verify(blockStorageGateway).create(any());
		}

		@Test
		void invalidSequenceId() {
			final long topicId = TOPIC.getId();

			when(topicService.getById(PRINCIPAL, topicId))
					.thenReturn(TOPIC.info());

			when(blockStorageGateway.findAllByTopicId(topicId))
					.thenReturn(Collections.emptyList());

			final var action = new BlockAction.Create(TOPIC.getId(),
					BLOCK.getType(), 1);

			assertThatThrownBy(() -> blockService.create(PRINCIPAL, action))
					.isInstanceOf(IllegalArgumentException.class);

			verifyNoInteractions(blockPublisher);
			verify(blockStorageGateway, never()).update(any());
			verify(blockStorageGateway, never()).create(any());
		}

		@Test
		void principalNull() {

			final var block = new BlockAction.Create(TOPIC.getId(),
					BLOCK.getType(), BLOCK.getSequenceId());

			assertThatThrownBy(() -> blockService.create(null, block))
					.isInstanceOf(NullPointerException.class);
		}

		@Test
		void actionNull() {

			assertThatThrownBy(() -> blockService.create(PRINCIPAL, null))
					.isInstanceOf(NullPointerException.class);
		}

	}

	@Nested
	class Move {

		private static final TestTopic TOPIC = TestTopic.SPORER_PROJECT_BLOCKERS;

		@Test
		void down() {
			final List<BlockInfo> existingBlocks = TestBlock.ofTopic(TOPIC)
					.stream()
					.map(TestBlock::info)
					.toList();

			final BlockInfo block = existingBlocks.stream()
					.filter(b -> b.getSequenceId() == 0)
					.findFirst()
					.orElseThrow();
			final BlockInfo updatedBlock = mock();

			final BlockInfo second = existingBlocks.stream()
					.filter(b -> b.getSequenceId() == 1)
					.findFirst()
					.orElseThrow();
			final BlockInfo updatedSecond = mock();

			final BlockInfo third = existingBlocks.stream()
					.filter(b -> b.getSequenceId() == 2)
					.findFirst()
					.orElseThrow();
			final BlockInfo updatedThird = mock();

			when(blockStorageGateway.find(block.getId()))
					.thenReturn(Optional.of(block));

			when(blockStorageGateway.findAllByTopicId(TOPIC.getId()))
					.thenReturn(existingBlocks);

			when(blockStorageGateway.update(any())).thenAnswer(invocation -> {
				var update = invocation.getArgument(0, BlockInfo.class);

				if (update.getId() == block.getId()
						&& update.getSequenceId() == 2) {
					return updatedBlock;
				}

				if (update.getId() == second.getId()
						&& update.getSequenceId() == 0) {
					return updatedSecond;
				}

				if (update.getId() == third.getId()
						&& update.getSequenceId() == 1) {
					return updatedThird;
				}

				throw new AssertionError("Unexpected update: " + update);
			});

			final var action = new BlockAction.Move(2);
			final BlockInfo result = blockService.move(PRINCIPAL, block.getId(),
					action);

			assertThat(result).isEqualTo(block);
			assertThat(result.getSequenceId()).isEqualTo(2);

			final var moved = List.of(result, second, third);

			final ArgumentCaptor<BlockEvent> events = ArgumentCaptor
					.forClass(BlockEvent.class);

			verify(blockPublisher, times(moved.size()))
					.publish(events.capture());

			final List<BlockEvent> blockEvents = events.getAllValues();
			assertThat(blockEvents).hasSameSizeAs(moved)
					.satisfiesExactlyInAnyOrder(event -> {
						assertThat(event.block()).isEqualTo(updatedBlock);
						final var expected = new BlockAction.Move(2);
						final var matcher = new BlockActionMatcher.Move(
								expected);
						assertThat(event.action()).is(matcher.equal());
					}, event -> {
						assertThat(event.block()).isEqualTo(updatedSecond);
						final var expected = new BlockAction.Move(0);
						final var matcher = new BlockActionMatcher.Move(
								expected);
						assertThat(event.action()).is(matcher.equal());
					}, event -> {
						assertThat(event.block()).isEqualTo(updatedThird);
						final var expected = new BlockAction.Move(1);
						final var matcher = new BlockActionMatcher.Move(
								expected);
						assertThat(event.action()).is(matcher.equal());
					});
		}

		@Test
		void up() {
			final List<BlockInfo> existingBlocks = TestBlock.ofTopic(TOPIC)
					.stream()
					.map(TestBlock::info)
					.toList();

			final BlockInfo block = existingBlocks.stream()
					.filter(b -> b.getSequenceId() == 2)
					.findFirst()
					.orElseThrow();
			final BlockInfo updatedBlock = mock();

			final BlockInfo second = existingBlocks.stream()
					.filter(b -> b.getSequenceId() == 1)
					.findFirst()
					.orElseThrow();
			final BlockInfo updatedSecond = mock();

			final BlockInfo first = existingBlocks.stream()
					.filter(b -> b.getSequenceId() == 0)
					.findFirst()
					.orElseThrow();
			final BlockInfo updatedFirst = mock();

			when(blockStorageGateway.find(block.getId()))
					.thenReturn(Optional.of(block));

			when(blockStorageGateway.findAllByTopicId(TOPIC.getId()))
					.thenReturn(existingBlocks);

			when(blockStorageGateway.update(any())).thenAnswer(invocation -> {
				var update = invocation.getArgument(0, BlockInfo.class);

				if (update.getId() == block.getId()
						&& update.getSequenceId() == 0) {
					return updatedBlock;
				}

				if (update.getId() == first.getId()
						&& update.getSequenceId() == 1) {
					return updatedFirst;
				}

				if (update.getId() == second.getId()
						&& update.getSequenceId() == 2) {
					return updatedSecond;
				}

				throw new AssertionError("Unexpected update: " + update);
			});

			final var action = new BlockAction.Move(0);
			final BlockInfo result = blockService.move(PRINCIPAL, block.getId(),
					action);

			assertThat(result).isEqualTo(block);
			assertThat(result.getSequenceId()).isZero();

			final var moved = List.of(result, second, first);
			final ArgumentCaptor<BlockEvent> events = ArgumentCaptor
					.forClass(BlockEvent.class);
			verify(blockPublisher, times(moved.size()))
					.publish(events.capture());

			final List<BlockEvent> blockEvents = events.getAllValues();
			assertThat(blockEvents).hasSameSizeAs(moved)
					.satisfiesExactlyInAnyOrder(event -> {
						assertThat(event.block()).isEqualTo(updatedBlock);
						final var expected = new BlockAction.Move(0);
						final var matcher = new BlockActionMatcher.Move(
								expected);
						assertThat(event.action()).is(matcher.equal());
					}, event -> {
						assertThat(event.block()).isEqualTo(updatedFirst);
						final var expected = new BlockAction.Move(1);
						final var matcher = new BlockActionMatcher.Move(
								expected);
						assertThat(event.action()).is(matcher.equal());
					}, event -> {
						assertThat(event.block()).isEqualTo(updatedSecond);
						final var expected = new BlockAction.Move(2);
						final var matcher = new BlockActionMatcher.Move(
								expected);
						assertThat(event.action()).is(matcher.equal());
					});
		}

		@Test
		void neighbour() {
			final List<BlockInfo> existingBlocks = TestBlock.ofTopic(TOPIC)
					.stream()
					.map(TestBlock::info)
					.toList();

			final BlockInfo block = existingBlocks.stream()
					.filter(b -> b.getSequenceId() == 1)
					.findFirst()
					.orElseThrow();
			final BlockInfo updatedBlock = mock();

			final BlockInfo neighbour = existingBlocks.stream()
					.filter(b -> b.getSequenceId() == 2)
					.findFirst()
					.orElseThrow();
			final BlockInfo updatedNeighbour = mock();

			when(blockStorageGateway.find(block.getId()))
					.thenReturn(Optional.of(block));

			when(blockStorageGateway.findAllByTopicId(TOPIC.getId()))
					.thenReturn(existingBlocks);

			when(blockStorageGateway.update(any())).thenAnswer(invocation -> {
				var update = invocation.getArgument(0, BlockInfo.class);

				if (update.getId() == block.getId()
						&& update.getSequenceId() == 2) {
					return updatedBlock;
				}

				if (update.getId() == neighbour.getId()
						&& update.getSequenceId() == 1) {
					return updatedNeighbour;
				}

				throw new AssertionError("Unexpected update: " + update);
			});

			final var action = new BlockAction.Move(2);
			final BlockInfo result = blockService.move(PRINCIPAL, block.getId(),
					action);

			assertThat(result).isEqualTo(block);
			assertThat(result.getSequenceId()).isEqualTo(2);

			final var moved = List.of(neighbour, result);
			final ArgumentCaptor<BlockEvent> events = ArgumentCaptor
					.forClass(BlockEvent.class);
			verify(blockPublisher, times(moved.size()))
					.publish(events.capture());

			final List<BlockEvent> blockEvents = events.getAllValues();
			assertThat(blockEvents).hasSameSizeAs(moved)
					.satisfiesExactlyInAnyOrder(event -> {
						assertThat(event.block()).isEqualTo(updatedBlock);
						final var expected = new BlockAction.Move(2);
						final var matcher = new BlockActionMatcher.Move(
								expected);
						assertThat(event.action()).is(matcher.equal());
					}, event -> {
						assertThat(event.block()).isEqualTo(updatedNeighbour);
						final var expected = new BlockAction.Move(1);
						final var matcher = new BlockActionMatcher.Move(
								expected);
						assertThat(event.action()).is(matcher.equal());
					});
		}

		@Test
		void unchanged() {
			final BlockInfo block = TestBlock.SPORER_PROJECT_BLOCKERS_FIRST
					.info();
			final long blockId = block.getId();
			when(blockStorageGateway.find(blockId))
					.thenReturn(Optional.of(block));

			final var action = new BlockAction.Move(block.getSequenceId());
			blockService.move(PRINCIPAL, blockId, action);

			verifyNoInteractions(blockPublisher);
			verify(blockStorageGateway, never()).update(any());
			verify(blockStorageGateway, never()).findAllByTopicId(anyLong());
		}

		@Test
		void sequenceIdTooLarge() {
			final List<BlockInfo> existingBlocks = TestBlock.ofTopic(TOPIC)
					.stream()
					.map(TestBlock::info)
					.toList();

			final BlockInfo block = existingBlocks.getFirst();
			final long blockId = block.getId();
			when(blockStorageGateway.find(blockId))
					.thenReturn(Optional.of(block));

			when(blockStorageGateway.findAllByTopicId(TOPIC.getId()))
					.thenReturn(existingBlocks);

			final var action = new BlockAction.Move(existingBlocks.size());

			assertThatThrownBy(
					() -> blockService.move(PRINCIPAL, blockId, action))
							.isInstanceOf(IllegalArgumentException.class);

			verifyNoInteractions(blockPublisher);
			verify(blockStorageGateway, never()).update(any());
		}

		@Test
		void notFound() {
			final long blockId = TestBlock.SPORER_PROJECT_BLOCKERS_FIRST
					.getId();
			when(blockStorageGateway.find(blockId))
					.thenReturn(Optional.empty());

			final var action = new BlockAction.Move(1);

			assertThatThrownBy(
					() -> blockService.move(PRINCIPAL, blockId, action))
							.isInstanceOf(MissingEntityException.class);

			verifyNoInteractions(blockPublisher);
			verify(blockStorageGateway, never()).update(any());
		}

		@Test
		void otherOrganisation() {
			final Principal principal = TestSession.ALISON_DACH_GLOVER
					.principal();

			final BlockInfo block = TestBlock.SPORER_PROJECT_BLOCKERS_FIRST
					.info();
			final long blockId = block.getId();
			final var action = new BlockAction.Move(1);

			assertThatThrownBy(
					() -> blockService.move(principal, blockId, action))
							.isInstanceOf(MissingEntityException.class);

			verifyNoInteractions(blockPublisher);
			verify(blockStorageGateway, never()).update(any());
		}

		@Test
		void principalNull() {
			final long blockId = TestBlock.SPORER_PROJECT_BLOCKERS_FIRST
					.getId();
			final var action = new BlockAction.Move(1);

			assertThatThrownBy(() -> blockService.move(null, blockId, action))
					.isInstanceOf(NullPointerException.class);
		}

		@Test
		void actionNull() {
			final long blockId = TestBlock.SPORER_PROJECT_BLOCKERS_FIRST
					.getId();

			assertThatThrownBy(
					() -> blockService.move(PRINCIPAL, blockId, null))
							.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class Delete {

		private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
		private static final TestBlock BLOCK = TestBlock.SPORER_PROJECT_BLOCKERS_FIRST;
		private static final TestTopic TOPIC = BLOCK.getTopic();

		@Test
		void onlyBlock() {
			final Principal principal = SESSION.principal();
			final long blockId = BLOCK.getId();
			final long topicId = TOPIC.getId();

			final BlockInfo blockInfo = BLOCK.info();
			when(blockStorageGateway.find(blockId))
					.thenReturn(Optional.of(blockInfo));

			when(blockStorageGateway.findAllByTopicId(topicId))
					.thenReturn(List.of(blockInfo));

			blockService.delete(principal, blockId);

			verify(blockStorageGateway).delete(blockInfo);
			verify(blockPublisher).publish(argThat(event -> {
				assertThat(event.block()).isEqualTo(blockInfo);
				assertThat(event.action())
						.isInstanceOf(BlockAction.Delete.class);
				return true;
			}));

			verify(blockStorageGateway, never()).update(any());
		}

		@Test
		void firstBlock() {
			final TestTopic topic = TestTopic.SPORER_PROJECT_BLOCKERS;
			final List<TestBlock> blocks = TestBlock.ofTopic(topic);
			final TestBlock deleted = blocks.getFirst();

			final Principal principal = SESSION.principal();
			final long blockId = deleted.getId();
			final long topicId = topic.getId();

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

			blockService.delete(principal, blockId);

			verify(blockStorageGateway).delete(blockInfo);
			verify(blockPublisher).publish(argThat(event -> {
				assertThat(event.block()).isEqualTo(blockInfo);
				assertThat(event.action())
						.isInstanceOf(BlockAction.Delete.class);
				return true;
			}));

			// TODO: should have been moved up!
			final List<BlockInfo> toUpdateBlocks = existingBlocks.stream()
					.filter(t -> t.getId() != topicId)
					.toList();
			toUpdateBlocks.forEach(b -> verify(blockStorageGateway).update(b));

		}

		@Test
		void lastBlock() {
			final TestTopic topic = TestTopic.SPORER_PROJECT_BLOCKERS;
			final List<TestBlock> blocks = TestBlock.ofTopic(topic);
			final TestBlock deleted = blocks.getLast();

			final Principal principal = SESSION.principal();
			final long blockId = deleted.getId();
			final long topicId = topic.getId();

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

			blockService.delete(principal, blockId);

			verify(blockStorageGateway).delete(blockInfo);
			verify(blockPublisher).publish(argThat(event -> {
				assertThat(event.action())
						.isInstanceOf(BlockAction.Delete.class);
				return true;
			}));

			verify(blockStorageGateway, never()).update(any());
		}

		@Test
		void notFound() {
			final TestTopic topic = TestTopic.SPORER_PROJECT_BLOCKERS;
			final List<TestBlock> blocks = TestBlock.ofTopic(topic);
			final TestBlock deleted = blocks.getLast();

			final Principal principal = SESSION.principal();
			final long blockId = deleted.getId();

			when(blockStorageGateway.find(blockId))
					.thenReturn(Optional.empty());

			assertThatThrownBy(() -> blockService.delete(principal, blockId))
					.isInstanceOf(MissingEntityException.class);

			verify(blockStorageGateway, never()).delete(any());
			verifyNoInteractions(blockPublisher);
		}

		@Test
		void principalNull() {
			final long blockId = BLOCK.getId();

			assertThatThrownBy(() -> blockService.delete(null, blockId))
					.isInstanceOf(NullPointerException.class);
		}
	}

}
