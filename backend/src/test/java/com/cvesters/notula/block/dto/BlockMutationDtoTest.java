package com.cvesters.notula.block.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.block.TestBlock;
import com.cvesters.notula.block.bdo.BlockAction;

class BlockMutationDtoTest {

	private static final TestBlock BLOCK = TestBlock.SPORER_PROJECT_BLOCKERS_FIRST;

	@Nested
	class Of {

		@Test
		void create() {
			final var action = new BlockAction.Create(BLOCK.getType(),
					BLOCK.getSequenceId());

			final var dto = BlockMutationDto.of(action);

			assertThat(dto).isInstanceOf(BlockMutationDto.Create.class);

			final var createDto = (BlockMutationDto.Create) dto;
			assertThat(createDto.getType()).isEqualTo(BLOCK.getTypeString());
			assertThat(createDto.getSequenceId())
					.isEqualTo(BLOCK.getSequenceId());
		}

		@Test
		void actionNull() {
			assertThatThrownBy(() -> BlockMutationDto.of(null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
