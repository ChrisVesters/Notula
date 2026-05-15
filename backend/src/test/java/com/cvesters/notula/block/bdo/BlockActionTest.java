package com.cvesters.notula.block.bdo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class BlockActionTest {

	@Nested
	class Create {

		@Test
		void success() {
			final BlockType type = BlockType.TEXT;
			final int sequenceId = 0;

			final var action = new BlockAction.Create(type, sequenceId);

			assertThat(action.getType()).isEqualTo(type);
			assertThat(action.getSequenceId()).isEqualTo(sequenceId);
		}

		@Test
		void typeNull() {
			final BlockType type = null;
			final int sequenceId = 0;

			assertThatThrownBy(() -> new BlockAction.Create(type, sequenceId))
					.isInstanceOf(NullPointerException.class);
		}

		@Test
		void sequenceIdNegative() {
			final BlockType type = BlockType.TEXT;
			final int sequenceId = -1;

			assertThatThrownBy(() -> new BlockAction.Create(type, sequenceId))
					.isInstanceOf(IllegalArgumentException.class);
		}
	}
}
