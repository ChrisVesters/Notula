package com.cvesters.notula.block;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import com.cvesters.notula.block.bdo.BlockInfo;
import com.cvesters.notula.block.dao.BlockDao;
import com.cvesters.notula.common.exception.MissingEntityException;
import com.cvesters.notula.organisation.TestOrganisation;
import com.cvesters.notula.topic.TestTopic;

class BlockStorageGatewayTest {

	private final BlockRepository blockRepository = mock();

	private final BlockStorageGateway gateway = new BlockStorageGateway(
			blockRepository);

	@Nested
	class Create {

		@Test
		void success() {
			final TestBlock block = TestBlock.SPORER_PROJECT_BLOCKERS_FIRST;
			final TestTopic topic = block.getTopic();
			final TestOrganisation organisation = topic.getOrganisation();

			final BlockInfo info = block.info();

			final BlockDao created = mock();
			final BlockInfo createdBdo = mock();
			when(created.toBdo()).thenReturn(createdBdo);

			when(blockRepository.save(argThat(dao -> {
				assertThat(dao.getId()).isNull();
				assertThat(dao.getOrganisationId())
						.isEqualTo(organisation.getId());
				assertThat(dao.getTopicId()).isEqualTo(topic.getId());
				assertThat(dao.getType()).isEqualTo(block.getTypeId());
				return true;
			}))).thenReturn(created);

			final BlockInfo result = gateway.create(info);

			assertThat(result).isEqualTo(createdBdo);
		}

		@Test
		void blockNull() {
			assertThatThrownBy(() -> gateway.create(null))
					.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class FindAllByTopicId {

		private static final long TOPIC_ID = 1L;

		@Test
		void single() {
			final BlockDao dao = mock();
			final BlockInfo bdo = mock();
			when(dao.toBdo()).thenReturn(bdo);

			when(blockRepository.findAllByTopicId(TOPIC_ID))
					.thenReturn(List.of(dao));

			final List<BlockInfo> result = gateway.findAllByTopicId(TOPIC_ID);

			assertThat(result).containsExactly(bdo);
		}

		@Test
		void multiple() {
			final BlockDao dao1 = mock();
			final BlockInfo bdo1 = mock();
			when(dao1.toBdo()).thenReturn(bdo1);

			final BlockDao dao2 = mock();
			final BlockInfo bdo2 = mock();
			when(dao2.toBdo()).thenReturn(bdo2);

			when(blockRepository.findAllByTopicId(TOPIC_ID))
					.thenReturn(List.of(dao1, dao2));

			final List<BlockInfo> result = gateway.findAllByTopicId(TOPIC_ID);

			assertThat(result).containsExactly(bdo1, bdo2);
		}

		@Test
		void notFound() {
			when(blockRepository.findAllByTopicId(Long.MAX_VALUE))
					.thenReturn(Collections.emptyList());

			assertThat(gateway.findAllByTopicId(Long.MAX_VALUE)).isEmpty();
		}
	}

	@Nested
	class UpdateAll {

		@Test
		void single() {
			final TestBlock block = TestBlock.SPORER_PROJECT_BLOCKERS_FIRST;
			final BlockInfo updateBdo = block.info();
			final List<BlockInfo> blocks = List.of(updateBdo);

			final BlockDao found = mock();
			when(blockRepository.findByTopicIdAndId(block.getTopic().getId(),
					block.getId())).thenReturn(Optional.of(found));

			final BlockDao updatedDao = mock();
			final BlockInfo updatedBdo = mock();
			when(updatedDao.toBdo()).thenReturn(updatedBdo);

			final List<BlockDao> updatedBlocks = List.of(updatedDao);
			when(blockRepository.saveAll(List.of(found)))
					.thenReturn(updatedBlocks);

			final var result = gateway.updateAll(blocks);

			assertThat(result).hasSize(1).contains(updatedBdo);

			final InOrder inOrder = inOrder(found, blockRepository);
			inOrder.verify(found).update(updateBdo);
			inOrder.verify(blockRepository).saveAll(List.of(found));
		}

		@Test
		void multiple() {
			final List<TestBlock> blocks = List.of(
					TestBlock.SPORER_PROJECT_BLOCKERS_FIRST,
					TestBlock.SPORER_PROJECT_BLOCKERS_SECOND);

			final List<BlockInfo> updateBdos = new ArrayList<>();
			final List<BlockDao> foundDaos = new ArrayList<>();
			final List<BlockDao> updatedDaos = new ArrayList<>();
			final List<BlockInfo> updatedBdos = new ArrayList<>();
			for (final TestBlock block : blocks) {
				final BlockInfo updateBdo = block.info();
				updateBdos.add(updateBdo);

				final BlockDao found = mock();
				when(blockRepository.findByTopicIdAndId(
						block.getTopic().getId(), block.getId()))
								.thenReturn(Optional.of(found));
				foundDaos.add(found);

				final BlockDao updatedDao = mock();
				final BlockInfo updatedBdo = mock();
				when(updatedDao.toBdo()).thenReturn(updatedBdo);

				updatedDaos.add(updatedDao);
				updatedBdos.add(updatedBdo);
			}

			when(blockRepository.saveAll(foundDaos))
					.thenReturn(updatedDaos);

			final var result = gateway.updateAll(updateBdos);

			assertThat(result).containsExactlyElementsOf(updatedBdos);
		}

		@Test
		void emptyList() {
			final List<BlockInfo> blocks = List.of();

			when(blockRepository
					.saveAll(argThat((List<BlockDao> list) -> list.isEmpty())))
							.thenReturn(List.of());

			final var result = gateway.updateAll(blocks);

			assertThat(result).isEmpty();

			verify(blockRepository).saveAll(anyIterable());
			verifyNoMoreInteractions(blockRepository);
		}

		@Test
		void notFound() {
			final TestBlock block = TestBlock.SPORER_PROJECT_BLOCKERS_FIRST;
			final List<BlockInfo> blocks = List.of(block.info());

			when(blockRepository.findByTopicIdAndId(block.getTopic().getId(),
					block.getId())).thenReturn(Optional.empty());

			assertThatThrownBy(() -> gateway.updateAll(blocks))
					.isInstanceOf(MissingEntityException.class);

			verify(blockRepository, never()).saveAll(anyIterable());
		}

		@Test
		void blocksNull() {
			final List<BlockInfo> blocks = null;

			assertThatThrownBy(() -> gateway.updateAll(blocks))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
