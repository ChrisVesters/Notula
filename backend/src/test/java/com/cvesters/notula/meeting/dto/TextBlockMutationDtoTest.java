package com.cvesters.notula.meeting.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.block.bdo.BlockInfo;
import com.cvesters.notula.common.domain.Origin;
import com.cvesters.notula.session.TestSession;
import com.cvesters.notula.textblock.bdo.TextBlockAction;
import com.cvesters.notula.textblock.bdo.TextBlockEvent;

import tools.jackson.databind.ObjectMapper;

class TextBlockMutationDtoTest {

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
	private static final UUID CLIENT_ID = UUID
			.fromString("3f9c1a44-1d2e-4a51-8b0c-2c7e9b1d4a06");
	private static final Origin ORIGIN = new Origin(SESSION.principal(),
			CLIENT_ID);

	private static final long BLOCK_ID = 9L;

	@Nested
	class Of {

		@Test
		void updateContent() {
			final BlockInfo block = mock();
			when(block.getId()).thenReturn(BLOCK_ID);

			final var action = new TextBlockAction.UpdateContent(4, 12,
					"Updated");
			final var event = new TextBlockEvent(block, action, ORIGIN);

			final var dto = TextBlockMutationDto.of(event);

			final var edit = new TextEditDto(4, 12, "Updated");
			final var expected = new TextBlockMutationDto.Edit(BLOCK_ID, edit);
			assertThat(dto).isEqualTo(expected);
		}

		@Test
		void eventNull() {
			assertThatThrownBy(() -> TextBlockMutationDto.of(null))
					.isInstanceOf(NullPointerException.class);
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
