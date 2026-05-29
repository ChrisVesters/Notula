package com.cvesters.notula.textblock.bdo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.block.TestBlock;
import com.cvesters.notula.textblock.TestTextBlock;

class TextBlockInfoTest {

	private static final TestTextBlock TEXT_BLOCK = TestTextBlock.SPORER_PROJECT_BLOCKERS_FIRST;
	private static final TestBlock BLOCK = TEXT_BLOCK.getBlock();

	@Nested
	class Constructor {

		@Test
		void success() {
			final var result = new TextBlockInfo(BLOCK.getId(),
					TEXT_BLOCK.getContent());

			assertThat(result.getBlockId()).isEqualTo(BLOCK.getId());
			assertThat(result.getContent()).isEqualTo(TEXT_BLOCK.getContent());
		}

		@Test
		void contentNull() {
			final long blockId = BLOCK.getId();

			assertThatThrownBy(() -> new TextBlockInfo(blockId, null))
					.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class SetContent {

		@Test
		void success() {
			final String updatedContent = "Updated content";
			final var block = new TextBlockInfo(BLOCK.getId(),
					TEXT_BLOCK.getContent());

			block.setContent(updatedContent);

			assertThat(block.getContent()).isEqualTo(updatedContent);
		}

		@Test
		void contentNull() {
			final var block = new TextBlockInfo(BLOCK.getId(),
					TEXT_BLOCK.getContent());

			assertThatThrownBy(() -> block.setContent(null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
