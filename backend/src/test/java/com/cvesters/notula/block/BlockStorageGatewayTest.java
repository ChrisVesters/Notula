package com.cvesters.notula.block;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
				assertThat(dao.getType()).isEqualTo(block.getType());
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
	class FindById {

		private static final TestBlock BLOCK = TestBlock.SPORER_PROJECT_BLOCKERS_FIRST;

		@Test
		void success() {
			final BlockDao dao = mock();
			final BlockInfo bdo = mock();
			when(dao.toBdo()).thenReturn(bdo);

			when(blockRepository.findById(BLOCK.getId()))
					.thenReturn(Optional.of(dao));

			final Optional<BlockInfo> result = gateway.find(BLOCK.getId());

			assertThat(result).contains(bdo);
		}

		@Test
		void notFound() {
			when(blockRepository.findById(BLOCK.getId()))
					.thenReturn(Optional.empty());

			assertThat(gateway.find(BLOCK.getId())).isEmpty();
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
	class Update {

		private static final TestBlock BLOCK = TestBlock.SPORER_PROJECT_BLOCKERS_FIRST;

		@Test
		void success() {
			final BlockDao blockDao = mock();

			when(blockRepository.findById(BLOCK.getId()))
					.thenReturn(Optional.of(blockDao));

			final BlockDao updatedDao = mock();
			final BlockInfo updatedBdo = mock();
			when(updatedDao.toBdo()).thenReturn(updatedBdo);

			when(blockRepository.save(blockDao)).thenReturn(updatedDao);

			final BlockInfo update = BLOCK.info();

			final BlockInfo result = gateway.update(update);

			assertThat(result).isEqualTo(updatedBdo);

			final InOrder inOrder = inOrder(blockDao, blockRepository);
			inOrder.verify(blockDao).update(update);
			inOrder.verify(blockRepository).save(blockDao);
		}

		@Test
		void notFound() {
			when(blockRepository.findById(BLOCK.getId()))
					.thenReturn(Optional.empty());

			final BlockInfo update = BLOCK.info();

			assertThatThrownBy(() -> gateway.update(update))
					.isInstanceOf(MissingEntityException.class);
		}

		@Test
		void blockNull() {
			assertThatThrownBy(() -> gateway.update(null))
					.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class Delete {

		private static final TestBlock BLOCK = TestBlock.SPORER_PROJECT_BLOCKERS_FIRST;

		@Test
		void success() {
			final BlockDao found = mock();
			when(blockRepository.findById(BLOCK.getId()))
					.thenReturn(Optional.of(found));

			final BlockInfo update = BLOCK.info();
			gateway.delete(update);

			final InOrder inOrder = inOrder(blockRepository);
			inOrder.verify(blockRepository).delete(found);
		}

		@Test
		void notFound() {
			when(blockRepository.findById(BLOCK.getId()))
					.thenReturn(Optional.empty());

			final BlockInfo update = BLOCK.info();
			assertThatThrownBy(() -> gateway.delete(update))
					.isInstanceOf(MissingEntityException.class);
		}

		@Test
		void blockNull() {
			assertThatThrownBy(() -> gateway.delete(null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
