package com.cvesters.notula.textblock.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.block.TestBlock;
import com.cvesters.notula.textblock.TestTextBlock;
import com.cvesters.notula.textblock.bdo.TextBlockInfo;

class TextBlockDaoTest {

	private static final TestTextBlock TEXT_BLOCK = TestTextBlock.SPORER_PROJECT_BLOCKERS_FIRST;
	private static final TestBlock BLOCK = TEXT_BLOCK.getBlock();

	@Nested
	class Constructor {

		@Test
		void success() {
			final var dao = new TextBlockDao(TEXT_BLOCK.info());

			assertThat(dao.getBlockId()).isEqualTo(BLOCK.getId());
			assertThat(dao.getContent()).isEqualTo(TEXT_BLOCK.getContent());
		}

		@Test
		void bdoNull() {
			assertThatThrownBy(() -> new TextBlockDao(null))
					.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class Update {

		private final TextBlockDao dao = new TextBlockDao(TEXT_BLOCK.info());

		@Test
		void success() {
			final String updatedContent = "Updated content";
			final var updated = new TextBlockInfo(BLOCK.getId(),
					updatedContent);

			dao.update(updated);

			assertThat(dao.getContent()).isEqualTo(updated.getContent());
		}

		@Test
		void bdoNull() {
			assertThatThrownBy(() -> dao.update(null))
					.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class ToBdo {

		private final TextBlockDao dao = new TextBlockDao(TEXT_BLOCK.info());

		@Test
		void success() {
			final TextBlockInfo bdo = dao.toBdo();

			assertThat(bdo.getBlockId()).isEqualTo(BLOCK.getId());
			assertThat(bdo.getContent()).isEqualTo(TEXT_BLOCK.getContent());
		}
	}
}
