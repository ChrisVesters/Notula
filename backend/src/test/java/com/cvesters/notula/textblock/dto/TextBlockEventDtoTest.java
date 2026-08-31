package com.cvesters.notula.textblock.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.block.bdo.BlockInfo;
import com.cvesters.notula.common.domain.Origin;
import com.cvesters.notula.common.dto.OriginDto;
import com.cvesters.notula.session.TestSession;
import com.cvesters.notula.textblock.bdo.TextBlockAction;
import com.cvesters.notula.textblock.dao.TextBlockEvent;

class TextBlockEventDtoTest {

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
	private static final UUID CLIENT_ID = UUID
			.fromString("3f9c1a44-1d2e-4a51-8b0c-2c7e9b1d4a08");
	private static final Origin ORIGIN = new Origin(SESSION.principal(),
			CLIENT_ID);

	@Nested
	class Constructor {

		private static final long BLOCK_ID = 32L;

		@Test
		void success() {
			final var action = new TextBlockAction.UpdateContent(3, 1, "New");
			final BlockInfo block = mock();
			when(block.getId()).thenReturn(BLOCK_ID);

			final var event = new TextBlockEvent(block, action, ORIGIN);

			final var dto = new TextBlockEventDto(event);

			assertThat(dto.getTarget()).isEqualTo("TEXT_BLOCK");
			assertThat(dto.getBlockId()).isEqualTo(BLOCK_ID);
			assertThat(dto.getMutation())
					.isInstanceOf(TextBlockMutationDto.UpdateContent.class);
			assertThat(dto.getOrigin()).isEqualTo(new OriginDto(ORIGIN));

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
