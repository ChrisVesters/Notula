package com.cvesters.notula.textblock.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.textblock.bdo.TextBlockAction;

class TextBlockMutationDtoTest {

	@Nested
	class Of {

		@Test
		void updateContent() {
			final int position = 4;
			final int length = 12;
			final String value = "Updated";
			final var action = new TextBlockAction.UpdateContent(position,
					length, value);

			final var dto = TextBlockMutationDto.of(action);

			assertThat(dto)
					.isInstanceOf(TextBlockMutationDto.UpdateContent.class);

			final var updateNameDto = (TextBlockMutationDto.UpdateContent) dto;
			assertThat(updateNameDto.getPosition()).isEqualTo(position);
			assertThat(updateNameDto.getLength()).isEqualTo(length);
			assertThat(updateNameDto.getValue()).isEqualTo(value);
		}

		@Test
		void actionNull() {
			assertThatThrownBy(() -> TextBlockMutationDto.of(null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
