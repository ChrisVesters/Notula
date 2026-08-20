package com.cvesters.notula.block.bdo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class BlockEventTest {

	@Nested
	class Constructor {

		private static final long TOPIC_ID = 32L;

		@Test
		void success() {
			final BlockInfo block = mock();
			final var action = new BlockAction.Create(TOPIC_ID, BlockType.TEXT,
					0);
			final var event = new BlockEvent(block, action);

			assertThat(event.block()).isEqualTo(block);
			assertThat(event.action()).isEqualTo(action);
		}

		@Test
		void blockNull() {
			final var action = new BlockAction.Create(TOPIC_ID, BlockType.TEXT,
					0);

			assertThatThrownBy(() -> new BlockEvent(null, action))
					.isInstanceOf(NullPointerException.class);
		}

		@Test
		void actionNull() {
			final BlockInfo block = mock();

			assertThatThrownBy(() -> new BlockEvent(block, null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
