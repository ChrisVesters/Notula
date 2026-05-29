package com.cvesters.notula.textblock.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.textblock.bdo.TextBlockAction;

class TextBlockEventTest {

	@Nested
	class Constructor {

		@Test
		void success() {
			final var action = new TextBlockAction.UpdateContent(0, 0, "New");
			final var event = new TextBlockEvent(1L, 3L, action);

			assertThat(event.topicId()).isEqualTo(1L);
			assertThat(event.blockId()).isEqualTo(3L);
			assertThat(event.action()).isEqualTo(action);
		}

		@Test
		void actionNull() {
			assertThatThrownBy(() -> new TextBlockEvent(1L, 3L, null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
