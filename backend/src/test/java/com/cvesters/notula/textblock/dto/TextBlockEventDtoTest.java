package com.cvesters.notula.textblock.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.block.bdo.BlockInfo;
import com.cvesters.notula.textblock.bdo.TextBlockAction;
import com.cvesters.notula.textblock.dao.TextBlockEvent;

class TextBlockEventDtoTest {

	@Nested
	class Constructor {

		private static final long BLOCK_ID = 32L;

		@Test
		void success() {
			final var action = new TextBlockAction.UpdateContent(3, 1, "New");
			final BlockInfo block = mock();
			when(block.getId()).thenReturn(BLOCK_ID);

			final var event = new TextBlockEvent(block, action);

			final var dto = new TextBlockEventDto(event);

			assertThat(dto.getTarget()).isEqualTo("TEXT_BLOCK");
			assertThat(dto.getBlockId()).isEqualTo(BLOCK_ID);
			assertThat(dto.getMutation())
					.isInstanceOf(TextBlockMutationDto.UpdateContent.class);

			final var mutation = (TextBlockMutationDto.UpdateContent) dto
					.getMutation();

			assertThat(mutation.getPosition()).isEqualTo(3);
			assertThat(mutation.getLength()).isEqualTo(1);
			assertThat(mutation.getValue()).isEqualTo("New");
		}

		@Test
		void eventNull() {
			assertThatThrownBy(() -> new TextBlockEventDto(null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
