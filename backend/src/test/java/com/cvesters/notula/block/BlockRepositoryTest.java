package com.cvesters.notula.block;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.context.jdbc.Sql;

import com.cvesters.notula.block.bdo.BlockInfo;
import com.cvesters.notula.block.bdo.BlockType;
import com.cvesters.notula.block.dao.BlockDao;
import com.cvesters.notula.organisation.TestOrganisation;
import com.cvesters.notula.test.RepositoryTest;
import com.cvesters.notula.topic.TestTopic;

@Sql({ "/db/organisations.sql", "/db/meetings.sql", "/db/topics.sql",
		"/db/blocks.sql" })
public class BlockRepositoryTest extends RepositoryTest {

	@Autowired
	private BlockRepository blockRepository;

	@Nested
	class FindAllByTopicId {

		@Test
		void single() {
			final TestTopic topic = TestTopic.SPORER_PROJECT_DELIVERABLES;
			final List<TestBlock> blocks = TestBlock.ofTopic(topic);

			final List<BlockDao> result = blockRepository
					.findAllByTopicId(topic.getId());

			assertThat(result).hasSize(1);
			blocks.forEach(block -> {
				assertThat(result).anySatisfy(b -> assertEquals(b, block));
			});
		}

		@Test
		void multiple() {
			final TestTopic topic = TestTopic.SPORER_PROJECT_BLOCKERS;
			final List<TestBlock> blocks = TestBlock.ofTopic(topic);

			final List<BlockDao> result = blockRepository
					.findAllByTopicId(topic.getId());

			assertThat(result).hasSize(3);
			blocks.forEach(block -> {
				assertThat(result).anySatisfy(b -> assertEquals(b, block));
			});
		}

		@Test
		void notFound() {
			assertThat(blockRepository.findAllByTopicId(Long.MAX_VALUE))
					.isEmpty();
		}
	}

	@Nested
	class FindById {

		@Test
		void found() {
			final TestBlock block = TestBlock.SPORER_PROJECT_BLOCKERS_FIRST;

			final var result = blockRepository.findById(block.getId());

			assertThat(result)
					.hasValueSatisfying(dao -> assertEquals(dao, block));
		}

		@Test
		void notFound() {
			final var result = blockRepository.findById(Long.MAX_VALUE);

			assertThat(result).isEmpty();
		}
	}

	@Nested
	class Save {

		@Test
		void success() {
			final TestTopic topic = TestTopic.GLOVER_KICKOFF_2026_LOOKBACK;
			final TestOrganisation organisation = topic.getOrganisation();

			final var bdo = new BlockInfo(organisation.getId(), topic.getId(),
					BlockType.TEXT, 0);
			final var dao = new BlockDao(bdo);

			final BlockDao saved = blockRepository.save(dao);

			final var matcher = new BlockDaoMatcher(entityManager, dao);
			assertThat(saved).is(matcher.created()).is(matcher.found());
		}

		@Test
		@Disabled("Currently not enforced")
		void overlappingSequenceId() {
			final TestBlock block = TestBlock.SPORER_PROJECT_BLOCKERS_FIRST;
			final TestTopic topic = block.getTopic();
			final TestOrganisation organisation = topic.getOrganisation();

			final var bdo = new BlockInfo(organisation.getId(), topic.getId(),
					BlockType.TEXT, 0);
			final var dao = new BlockDao(bdo);

			assertThatThrownBy(() -> blockRepository.save(dao))
					.isInstanceOf(DataIntegrityViolationException.class);
		}

		@Test
		void blockNull() {
			final BlockDao dao = null;

			assertThatThrownBy(() -> blockRepository.save(dao))
					.isInstanceOf(InvalidDataAccessApiUsageException.class);
		}

	}

	@Nested
	class Delete {

		@Test
		void success() {
			final TestBlock block = TestBlock.SPORER_PROJECT_BLOCKERS_THIRD;
			final BlockDao dao = entityManager.find(BlockDao.class,
					block.getId());

			blockRepository.delete(dao);

			final BlockDao deleted = entityManager.find(BlockDao.class,
					block.getId());
			assertThat(deleted).isNull();
		}

		@Test
		void topicNull() {
			assertThatThrownBy(() -> blockRepository.delete(null))
					.isInstanceOf(InvalidDataAccessApiUsageException.class);
		}

		@Test
		void nonExisting() throws Exception {
			final TestBlock block = TestBlock.SPORER_PROJECT_BLOCKERS_THIRD;
			final BlockDao dao = new BlockDao(block.info());

			final Field idField = dao.getClass().getDeclaredField("id");
			idField.setAccessible(true);
			idField.set(dao, Long.MAX_VALUE);

			assertThatCode(() -> blockRepository.delete(dao))
					.doesNotThrowAnyException();

			final BlockDao found = entityManager.find(BlockDao.class,
					Long.MAX_VALUE);

			assertThat(found).isNull();
		}
	}

	private static void assertEquals(final BlockDao dao,
			final TestBlock block) {
		assertThat(dao.getId()).isEqualTo(block.getId());
		assertThat(dao.getOrganisationId()).isEqualTo(
				block.getTopic().getMeeting().getOrganisation().getId());
		assertThat(dao.getTopicId()).isEqualTo(block.getTopic().getId());
		assertThat(dao.getType()).isEqualTo(block.getType());
		assertThat(dao.getSequenceId()).isEqualTo(block.getSequenceId());
	}
}
