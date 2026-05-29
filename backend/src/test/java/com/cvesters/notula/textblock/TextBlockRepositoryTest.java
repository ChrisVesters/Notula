package com.cvesters.notula.textblock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.context.jdbc.Sql;

import com.cvesters.notula.block.TestBlock;
import com.cvesters.notula.test.RepositoryTest;
import com.cvesters.notula.textblock.bdo.TextBlockInfo;
import com.cvesters.notula.textblock.dao.TextBlockDao;

@Sql({ "/db/organisations.sql", "/db/meetings.sql", "/db/topics.sql",
		"/db/blocks.sql", "/db/text_blocks.sql" })
class TextBlockRepositoryTest extends RepositoryTest {

	@Autowired
	private TextBlockRepository blockRepository;

	@Nested
	class FindByBlockId {

		@Test
		void single() {
			final TestTextBlock textBlock = TestTextBlock.SPORER_PROJECT_BLOCKERS_FIRST;
			final TestBlock block = textBlock.getBlock();

			final Optional<TextBlockDao> result = blockRepository
					.findByBlockId(block.getId());

			assertThat(result).isPresent()
					.hasValueSatisfying(b -> assertEquals(b, textBlock));
		}

		@Test
		void notFound() {
			assertThat(blockRepository.findByBlockId(Long.MAX_VALUE)).isEmpty();
		}
	}

	@Nested
	class Save {

		@Test
		void success() {
			final TestBlock block = TestBlock.SPORER_PROJECT_DELIVERABLES_FIRST;
			final String content = "1. Documentation";
			final var bdo = new TextBlockInfo(block.getId(), content);
			final var dao = new TextBlockDao(bdo);

			final TextBlockDao saved = blockRepository.save(dao);

			assertThat(saved.getBlockId()).isEqualTo(block.getId());
			assertThat(saved.getContent()).isEqualTo(content);

			final TextBlockDao found = entityManager.find(TextBlockDao.class,
					saved.getBlockId());
			assertThat(found).isNotNull();
			assertThat(found.getBlockId()).isEqualTo(saved.getBlockId());
			assertThat(found.getContent()).isEqualTo(saved.getContent());
		}

		@Test
		void topicNull() {
			assertThatThrownBy(() -> blockRepository.save(null))
					.isInstanceOf(InvalidDataAccessApiUsageException.class);
		}
	}

	private static void assertEquals(final TextBlockDao dao,
			final TestTextBlock block) {
		assertThat(dao.getBlockId()).isEqualTo(block.getBlock().getId());
		assertThat(dao.getContent()).isEqualTo(block.getContent());
	}
}
