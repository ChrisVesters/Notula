package com.cvesters.notula.textblock.bdo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.cvesters.notula.common.domain.Splice;

class TextBlockActionTest {
	
	@Nested
	class UpdateContent {

		@ParameterizedTest
		@CsvSource({ "0,4,'Updated',Test,Updated", "1,4,2026,M2025,M2026",
				"2,2,tr,Rexxospective,Retrospective" })
		void success(final int position, final int length, final String value,
				final String original, final String expected) {
			final var edit = new Splice(position, length, value);

			final var action = new TextBlockAction.UpdateContent(edit);

			assertThat(action.getEdit()).isEqualTo(edit);

			final TextBlockInfo block = mock();
			when(block.getContent()).thenReturn(original);

			action.apply(block);

			verify(block).setContent(expected);
		}

	}
}
