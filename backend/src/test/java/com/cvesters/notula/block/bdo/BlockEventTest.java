package com.cvesters.notula.block.bdo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class BlockEventTest {

	@Nested
	class Constructor {

		private static final long TOPIC_ID = 32L;
		private static final long BLOCK_ID = 61L;

		@Test
		void success() {
			final var action = new BlockAction.Create(BlockType.TEXT, 0);
			final var event = new BlockEvent(TOPIC_ID, BLOCK_ID,
					action);

			assertThat(event.topicId()).isEqualTo(TOPIC_ID);
			assertThat(event.blockId()).isEqualTo(BLOCK_ID);
			assertThat(event.action()).isEqualTo(action);
		}

		@Test
		void actionNull() {
			assertThatThrownBy(
					() -> new BlockEvent(TOPIC_ID, BLOCK_ID, null))
							.isInstanceOf(NullPointerException.class);
		}
	}
}
