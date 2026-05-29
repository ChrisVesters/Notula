package com.cvesters.notula.details.bdo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.textblock.TestTextBlock;
import com.cvesters.notula.textblock.bdo.TextBlockInfo;

class BlockContentTest {

	@Nested
	class Text {

		@Test
		void empty() {
			final var text = new BlockContent.Text();

			assertThat(text.content()).isEmpty();
		}

		@Test
		void content() {
			final String content = "content";

			final var text = new BlockContent.Text(content);

			assertThat(text.content()).isEqualTo(content);
		}

		@Test
		void info() {
			final TestTextBlock textBlock = TestTextBlock.SPORER_PROJECT_BLOCKERS_FIRST;
			final TextBlockInfo textBlockInfo = textBlock.info();

			final var text = new BlockContent.Text(textBlockInfo);

			assertThat(text.content()).isEqualTo(textBlock.getContent());
		}

		@Test
		void contentNull() {
			final String content = null;

			assertThatThrownBy(() -> new BlockContent.Text(content))
					.isInstanceOf(NullPointerException.class);
		}

		@Test
		void infoNull() {
			final TextBlockInfo textBlockInfo = null;

			assertThatThrownBy(() -> new BlockContent.Text(textBlockInfo))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
