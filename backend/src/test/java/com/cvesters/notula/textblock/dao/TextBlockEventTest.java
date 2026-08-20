package com.cvesters.notula.textblock.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.block.bdo.BlockInfo;
import com.cvesters.notula.textblock.bdo.TextBlockAction;

class TextBlockEventTest {

	@Nested
	class Constructor {

		@Test
		void success() {
			final BlockInfo block = mock();
			final var action = new TextBlockAction.UpdateContent(0, 0, "New");
			final var event = new TextBlockEvent(block, action);

			assertThat(event.block()).isEqualTo(block);
			assertThat(event.action()).isEqualTo(action);
		}

		@Test
		void blockNull() {
			final var action = new TextBlockAction.UpdateContent(0, 0, "New");

			assertThatThrownBy(() -> new TextBlockEvent(null, action))
					.isInstanceOf(NullPointerException.class);
		}

		@Test
		void actionNull() {
			final BlockInfo block = mock();

			assertThatThrownBy(() -> new TextBlockEvent(block, null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
