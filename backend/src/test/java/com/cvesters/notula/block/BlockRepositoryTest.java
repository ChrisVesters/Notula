package com.cvesters.notula.block;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Field;
import java.util.ArrayList;
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
	class FindByTopicIdAndId {

		@Test
		void found() {
			final TestBlock block = TestBlock.SPORER_PROJECT_BLOCKERS_FIRST;

			final var result = blockRepository.findByTopicIdAndId(
					block.getTopic().getId(), block.getId());

			assertThat(result)
					.hasValueSatisfying(dao -> assertEquals(dao, block));
		}

		@Test
		void blockNotFound() {
			final TestBlock block = TestBlock.SPORER_PROJECT_BLOCKERS_FIRST;
			final TestTopic topic = block.getTopic();

			final var result = blockRepository.findByTopicIdAndId(topic.getId(),
					Long.MAX_VALUE);

			assertThat(result).isEmpty();
		}

		@Test
		void topicNotFound() {
			final TestBlock block = TestBlock.SPORER_PROJECT_BLOCKERS_FIRST;

			final var result = blockRepository
					.findByTopicIdAndId(Long.MAX_VALUE, block.getId());

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
	class SaveAll {

		@Test
		void singleInList() {
			final TestTopic topic = TestTopic.GLOVER_KICKOFF_2026_LOOKBACK;
			final TestOrganisation organisation = topic.getOrganisation();

			final var bdo = new BlockInfo(organisation.getId(), topic.getId(),
					BlockType.TEXT, 0);
			final var dao = new BlockDao(bdo);
			final var daos = List.of(dao);

			final List<BlockDao> saved = blockRepository.saveAll(daos);

			assertThat(saved).hasSameSizeAs(daos)
					.zipSatisfy(daos, (savedDao, expectedDao) -> {
						final var matcher = new BlockDaoMatcher(entityManager,
								expectedDao);

						assertThat(savedDao).is(matcher.created())
								.is(matcher.found());
					});
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
		void multiple() {
			final TestTopic topic = TestTopic.GLOVER_KICKOFF_2026_LOOKBACK;
			final TestOrganisation organisation = topic.getOrganisation();

			final var bdo1 = new BlockInfo(organisation.getId(), topic.getId(),
					BlockType.TEXT, 0);
			final var dao1 = new BlockDao(bdo1);

			final var bdo2 = new BlockInfo(organisation.getId(), topic.getId(),
					BlockType.TEXT, 1);
			final var dao2 = new BlockDao(bdo2);
			final var daos = List.of(dao1, dao2);

			final List<BlockDao> saved = blockRepository.saveAll(daos);

			assertThat(saved).hasSameSizeAs(daos)
					.zipSatisfy(daos, (savedDao, expectedDao) -> {
						final var matcher = new BlockDaoMatcher(entityManager,
								expectedDao);

						assertThat(savedDao).is(matcher.created())
								.is(matcher.found());
					});
		}

		@Test
		@Disabled("Currently not enforced")
		void sameSequenceId() {
			final TestTopic topic = TestTopic.GLOVER_KICKOFF_2026_LOOKBACK;
			final TestOrganisation organisation = topic.getOrganisation();

			final var bdo1 = new BlockInfo(organisation.getId(), topic.getId(),
					BlockType.TEXT, 0);
			final var dao1 = new BlockDao(bdo1);

			final var bdo2 = new BlockInfo(organisation.getId(), topic.getId(),
					BlockType.TEXT, 0);
			final var dao2 = new BlockDao(bdo2);
			final var daos = List.of(dao1, dao2);

			assertThatThrownBy(() -> blockRepository.saveAll(daos))
					.isInstanceOf(DataIntegrityViolationException.class);
		}

		@Test
		@Disabled("Currently not enforced")
		void multipleWithOverlappingSequenceId() {
			final TestTopic topic = TestTopic.SPORER_PROJECT_BLOCKERS;
			final TestOrganisation organisation = topic.getOrganisation();

			final var bdo1 = new BlockInfo(organisation.getId(), topic.getId(),
					BlockType.TEXT, 0);
			final var dao1 = new BlockDao(bdo1);

			final var bdo2 = new BlockInfo(organisation.getId(), topic.getId(),
					BlockType.TEXT, 23);
			final var dao2 = new BlockDao(bdo2);
			final var daos = List.of(dao1, dao2);

			assertThatThrownBy(() -> blockRepository.saveAll(daos))
					.isInstanceOf(DataIntegrityViolationException.class);
		}

		@Test
		void multipleWithUpdateSequenceId() throws Exception {
			final TestTopic topic = TestTopic.SPORER_PROJECT_BLOCKERS;
			final TestOrganisation organisation = topic.getOrganisation();

			final var bdo1 = new BlockInfo(organisation.getId(), topic.getId(),
			BlockType.TEXT, 2);
			final var dao1 = new BlockDao(bdo1);
			
			final TestBlock existingBlock = TestBlock.SPORER_PROJECT_BLOCKERS_THIRD;
			final var bdo2 = existingBlock.info();
			bdo2.moveDown();
			final var dao2 = new BlockDao(bdo2);
			final Field idField = dao2.getClass().getDeclaredField("id");
			idField.setAccessible(true);
			idField.set(dao2, existingBlock.getId());
			
			final var daos = List.of(dao1, dao2);

			final List<BlockDao> saved = blockRepository.saveAll(daos);

			assertThat(saved).hasSameSizeAs(daos)
					.zipSatisfy(daos, (savedDao, expectedDao) -> {
						final var matcher = new BlockDaoMatcher(entityManager,
								expectedDao);

						assertThat(savedDao).is(matcher.created())
								.is(matcher.found());
					});
		}

		@Test
		void listNull() {
			final List<BlockDao> dao = null;

			assertThatThrownBy(() -> blockRepository.saveAll(dao))
					.isInstanceOf(InvalidDataAccessApiUsageException.class);
		}

		@Test
		void listContainsNull() {
			final var dao = new ArrayList<BlockDao>();
			dao.add(null);

			assertThatThrownBy(() -> blockRepository.saveAll(dao))
					.isInstanceOf(InvalidDataAccessApiUsageException.class);
		}
	}

	private static void assertEquals(final BlockDao dao,
			final TestBlock block) {
		assertThat(dao.getId()).isEqualTo(block.getId());
		assertThat(dao.getOrganisationId()).isEqualTo(
				block.getTopic().getMeeting().getOrganisation().getId());
		assertThat(dao.getTopicId()).isEqualTo(block.getTopic().getId());
		assertThat(dao.getType()).isEqualTo(block.getTypeId());
		assertThat(dao.getSequenceId()).isEqualTo(block.getSequenceId());
	}
}
