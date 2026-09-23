package com.cvesters.notula.block.bdo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class BlockActionTest {

	@Nested
	class Create {

		private static final long TOPIC_ID = 32L;
		private static final Long AFTER_ID = 61L;

		@Test
		void success() {
			final BlockType type = BlockType.TEXT;

			final var action = new BlockAction.Create(TOPIC_ID, type, AFTER_ID);

			assertThat(action.getTopicId()).isEqualTo(TOPIC_ID);
			assertThat(action.getType()).isEqualTo(type);
			assertThat(action.getAfterId()).contains(AFTER_ID);
		}

		@Test
		void first() {
			final var action = new BlockAction.Create(TOPIC_ID, BlockType.TEXT,
					null);

			assertThat(action.getTopicId()).isEqualTo(TOPIC_ID);
			assertThat(action.getType()).isEqualTo(BlockType.TEXT);
			assertThat(action.getAfterId()).isEmpty();
		}

		@Test
		void typeNull() {
			assertThatThrownBy(
					() -> new BlockAction.Create(TOPIC_ID, null, AFTER_ID))
							.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class Move {

		@Test
		void constructor() {
			final var action = new BlockAction.Move(61L);

			assertThat(action.getAfterId()).contains(61L);
		}

		@Test
		void first() {
			final var action = new BlockAction.Move(null);

			assertThat(action.getAfterId()).isEmpty();
		}
	}

	@Nested
	class Delete {

		@Test
		void success() {
			final var action = new BlockAction.Delete();

			assertThat(action).isNotNull();
		}
	}
}
