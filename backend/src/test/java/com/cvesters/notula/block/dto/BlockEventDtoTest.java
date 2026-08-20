package com.cvesters.notula.block.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.block.TestBlock;
import com.cvesters.notula.block.bdo.BlockAction;
import com.cvesters.notula.block.bdo.BlockEvent;
import com.cvesters.notula.block.bdo.BlockInfo;

class BlockEventDtoTest {

	private static final TestBlock BLOCK = TestBlock.SPORER_PROJECT_BLOCKERS_FIRST;

	@Nested
	class Constructor {

		private static final long TOPIC_ID = 32L;
		private static final long BLOCK_ID = 61L;

		@Test
		void success() {
			final BlockInfo block = mock();
			when(block.getId()).thenReturn(BLOCK_ID);

			final var action = new BlockAction.Create(TOPIC_ID,
					BLOCK.getType(), BLOCK.getSequenceId());
			final var event = new BlockEvent(block, action);

			final var dto = new BlockEventDto(event);

			assertThat(dto.getTarget()).isEqualTo("BLOCK");
			assertThat(dto.getBlockId()).isEqualTo(BLOCK_ID);
			assertThat(dto.getMutation())
					.isInstanceOf(BlockMutationDto.Create.class);

			final var mutation = (BlockMutationDto.Create) dto.getMutation();
			assertThat(mutation.getTopicId()).isEqualTo(TOPIC_ID);
			assertThat(mutation.getType()).isEqualTo(BLOCK.getTypeDto());
			assertThat(mutation.getSequenceId())
					.isEqualTo(BLOCK.getSequenceId());
		}

		@Test
		void eventNull() {
			assertThatThrownBy(() -> new BlockEventDto(null))
					.isInstanceOf(NullPointerException.class);
		}
	}

}
