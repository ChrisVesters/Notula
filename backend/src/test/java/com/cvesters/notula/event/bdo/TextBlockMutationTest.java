package com.cvesters.notula.event.bdo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.common.domain.Splice;

class TextBlockMutationTest {

	private static final long BLOCK_ID = 61L;
	private static final Splice EDIT = new Splice(4, 12, "Updated");

	@Nested
	class Edit {

		@Test
		void success() {
			final var mutation = new TextBlockMutation.Edit(BLOCK_ID, EDIT);

			assertThat(mutation.blockId()).isEqualTo(BLOCK_ID);
			assertThat(mutation.edit()).isEqualTo(EDIT);
		}

		@Test
		void editNull() {
			assertThatThrownBy(() -> new TextBlockMutation.Edit(BLOCK_ID, null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
