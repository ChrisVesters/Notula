package com.cvesters.notula.event.bdo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.block.bdo.BlockType;
import com.cvesters.notula.common.domain.Rank;

class BlockMutationTest {

	private static final long BLOCK_ID = 61L;
	private static final long TOPIC_ID = 32L;
	private static final Rank RANK = new Rank("b2");

	@Nested
	class Add {

		@Test
		void success() {
			final var mutation = new BlockMutation.Add(BLOCK_ID, TOPIC_ID,
					BlockType.TEXT, RANK);

			assertThat(mutation.blockId()).isEqualTo(BLOCK_ID);
			assertThat(mutation.topicId()).isEqualTo(TOPIC_ID);
			assertThat(mutation.type()).isEqualTo(BlockType.TEXT);
			assertThat(mutation.rank()).isEqualTo(RANK);
		}

		@Test
		void typeNull() {
			assertThatThrownBy(
					() -> new BlockMutation.Add(BLOCK_ID, TOPIC_ID, null, RANK))
							.isInstanceOf(NullPointerException.class);
		}

		@Test
		void rankNull() {
			assertThatThrownBy(() -> new BlockMutation.Add(BLOCK_ID, TOPIC_ID,
					BlockType.TEXT, null))
							.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class Move {

		@Test
		void success() {
			final var mutation = new BlockMutation.Move(BLOCK_ID, RANK);

			assertThat(mutation.blockId()).isEqualTo(BLOCK_ID);
			assertThat(mutation.rank()).isEqualTo(RANK);
		}

		@Test
		void rankNull() {
			assertThatThrownBy(() -> new BlockMutation.Move(BLOCK_ID, null))
					.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class Remove {

		@Test
		void success() {
			final var mutation = new BlockMutation.Remove(BLOCK_ID);

			assertThat(mutation.blockId()).isEqualTo(BLOCK_ID);
		}
	}
}
