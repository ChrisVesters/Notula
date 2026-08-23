package com.cvesters.notula.block.bdo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class BlockActionTest {

	@Nested
	class Create {

		private static final long TOPIC_ID = 32L;

		@Test
		void success() {
			final BlockType type = BlockType.TEXT;
			final int sequenceId = 0;

			final var action = new BlockAction.Create(TOPIC_ID, type,
					sequenceId);

			assertThat(action.getTopicId()).isEqualTo(TOPIC_ID);
			assertThat(action.getType()).isEqualTo(type);
			assertThat(action.getSequenceId()).isEqualTo(sequenceId);
		}

		@Test
		void typeNull() {
			final BlockType type = null;
			final int sequenceId = 0;

			assertThatThrownBy(
					() -> new BlockAction.Create(TOPIC_ID, type, sequenceId))
							.isInstanceOf(NullPointerException.class);
		}

		@Test
		void sequenceIdNegative() {
			final BlockType type = BlockType.TEXT;
			final int sequenceId = -1;

			assertThatThrownBy(
					() -> new BlockAction.Create(TOPIC_ID, type, sequenceId))
							.isInstanceOf(IllegalArgumentException.class);
		}
	}

	@Nested
	class Move {

		@Test
		void constructor() {
			final int sequenceId = 2;

			final var action = new BlockAction.Move(sequenceId);

			assertThat(action.getSequenceId()).isEqualTo(sequenceId);
		}

		@Test
		void sequenceIdNegative() {
			final int sequenceId = -1;

			assertThatThrownBy(() -> new BlockAction.Move(sequenceId))
					.isInstanceOf(IllegalArgumentException.class);
		}

		@Test
		void apply() {
			final int sequenceId = 2;
			final BlockInfo block = mock();

			final var action = new BlockAction.Move(sequenceId);

			action.apply(block);

			verify(block).setSequenceId(sequenceId);
		}

		@Test
		void blockNull() {
			final var action = new BlockAction.Move(0);

			assertThatThrownBy(() -> action.apply(null))
					.isInstanceOf(NullPointerException.class);
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
