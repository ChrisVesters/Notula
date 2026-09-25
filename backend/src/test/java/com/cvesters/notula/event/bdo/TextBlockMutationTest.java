package com.cvesters.notula.event.bdo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class TextBlockMutationTest {

	private static final long BLOCK_ID = 61L;

	@Nested
	class Edit {

		@Test
		void success() {
			final var mutation = new TextBlockMutation.Edit(BLOCK_ID, 4, 12,
					"Updated");

			assertThat(mutation.blockId()).isEqualTo(BLOCK_ID);
			assertThat(mutation.position()).isEqualTo(4);
			assertThat(mutation.length()).isEqualTo(12);
			assertThat(mutation.value()).isEqualTo("Updated");
		}

		@Test
		void positionNegative() {
			assertThatThrownBy(
					() -> new TextBlockMutation.Edit(BLOCK_ID, -1, 12, "Up"))
							.isInstanceOf(IllegalArgumentException.class);
		}

		@Test
		void lengthNegative() {
			assertThatThrownBy(
					() -> new TextBlockMutation.Edit(BLOCK_ID, 4, -1, "Up"))
							.isInstanceOf(IllegalArgumentException.class);
		}

		@Test
		void valueNull() {
			assertThatThrownBy(
					() -> new TextBlockMutation.Edit(BLOCK_ID, 4, 12, null))
							.isInstanceOf(NullPointerException.class);
		}
	}
}
