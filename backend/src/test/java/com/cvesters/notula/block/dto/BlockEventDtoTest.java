package com.cvesters.notula.block.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.block.TestBlock;
import com.cvesters.notula.block.bdo.BlockAction;
import com.cvesters.notula.block.bdo.BlockEvent;

class BlockEventDtoTest {

	private static final TestBlock BLOCK = TestBlock.SPORER_PROJECT_BLOCKERS_FIRST;

	@Nested
	class Constructor {

		private static final long TOPIC_ID = 32L;
		private static final long BLOCK_ID = 61L;

		@Test
		void success() {
			final var action = new BlockAction.Create(BLOCK.getType(),
					BLOCK.getSequenceId());
			final var event = new BlockEvent(TOPIC_ID, BLOCK_ID, action);

			final var dto = new BlockEventDto(event);

			assertThat(dto.getTarget()).isEqualTo("BLOCK");
			assertThat(dto.getTopicId()).isEqualTo(TOPIC_ID);
			assertThat(dto.getBlockId()).isEqualTo(BLOCK_ID);
			assertThat(dto.getMutation())
					.isInstanceOf(BlockMutationDto.Create.class);

			final var mutation = (BlockMutationDto.Create) dto.getMutation();
			assertThat(mutation.getType()).isEqualTo(BLOCK.getTypeString());
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
