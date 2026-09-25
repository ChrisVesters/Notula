package com.cvesters.notula.meeting.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.block.TestBlock;
import com.cvesters.notula.common.domain.Splice;
import com.cvesters.notula.textblock.TextBlockActionMatcher;
import com.cvesters.notula.textblock.bdo.TextBlockAction;

class TextBlockChangeDtoTest {

	private static final TestBlock BLOCK = TestBlock.SPORER_PROJECT_BLOCKERS_FIRST;

	@Nested
	class Edit {

		private static final TextEditDto EDIT = new TextEditDto(4, 12,
				"Updated");

		@Test
		void toBdo() {
			final var dto = new TextBlockChangeDto.Edit(BLOCK.getId(), EDIT);

			final TextBlockAction.UpdateContent bdo = dto.toBdo();

			final var expected = new TextBlockAction.UpdateContent(
					new Splice(4, 12, "Updated"));
			final var matcher = new TextBlockActionMatcher.UpdateContent(
					expected);
			assertThat(bdo).is(matcher.equal());
		}
	}
}
