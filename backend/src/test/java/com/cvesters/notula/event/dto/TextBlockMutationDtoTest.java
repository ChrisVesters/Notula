package com.cvesters.notula.event.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.common.domain.Splice;
import com.cvesters.notula.event.bdo.TextBlockMutation;
import com.cvesters.notula.meeting.dto.TextEditDto;

import tools.jackson.databind.ObjectMapper;

class TextBlockMutationDtoTest {

	private static final long BLOCK_ID = 9L;

	@Nested
	class Of {

		@Test
		void edit() {
			final var mutation = new TextBlockMutation.Edit(BLOCK_ID,
					new Splice(4, 12, "Updated"));

			final var dto = TextBlockMutationDto.of(mutation);

			final var edit = new TextEditDto(4, 12, "Updated");
			final var expected = new TextBlockMutationDto.Edit(BLOCK_ID, edit);
			assertThat(dto).isEqualTo(expected);
		}

		@Test
		void mutationNull() {
			assertThatThrownBy(() -> TextBlockMutationDto.of(null))
					.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class ToBdo {

		@Test
		void edit() {
			final var edit = new TextEditDto(4, 12, "Updated");
			final var dto = new TextBlockMutationDto.Edit(BLOCK_ID, edit);

			final var expected = new TextBlockMutation.Edit(BLOCK_ID,
					new Splice(4, 12, "Updated"));
			assertThat(dto.toBdo()).isEqualTo(expected);
		}
	}

	@Nested
	class Serialise {

		private static final ObjectMapper MAPPER = new ObjectMapper();

		@Test
		void edit() {
			final var edit = new TextEditDto(4, 12, "Updated");
			final MutationDto mutation = new TextBlockMutationDto.Edit(BLOCK_ID,
					edit);

			final String json = MAPPER.writeValueAsString(mutation);

			assertThat(json).isEqualToIgnoringWhitespace("""
					{
						"type": "EDIT_TEXT_BLOCK",
						"block": 9,
						"position": 4,
						"length": 12,
						"value": "Updated"
					}
					""");
		}
	}
}
