package com.cvesters.notula.textblock.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.textblock.TextBlockActionMatcher;
import com.cvesters.notula.textblock.bdo.TextBlockAction;

class TextBlockActionDtoTest {

	@Nested
	class UpdateContent {

		@Test
		void toBdo() {
			final var dto = new TextBlockActionDto.Update.Content(5, 2,
					"Updated");
			final TextBlockAction.Update bdo = dto.toBdo();

			final var expected = new TextBlockAction.UpdateContent(5, 2,
					"Updated");
			final var matcher = new TextBlockActionMatcher.UpdateContent(
					expected);
			assertThat(bdo).is(matcher.equal());
		}
	}
}
