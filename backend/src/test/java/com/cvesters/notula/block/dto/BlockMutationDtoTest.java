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
			assertThat(createDto.type()).isEqualTo(BLOCK.getTypeString());
			assertThat(createDto.sequenceId()).isEqualTo(BLOCK.getSequenceId());
		}

		@Test
		void actionNull() {
			assertThatThrownBy(() -> BlockMutationDto.of(null))
					.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class Create {

		final

		@Test void defaultConstructor() {
			final String type = BLOCK.getTypeString();
			final int sequenceId = BLOCK.getSequenceId();

			final var dto = new BlockMutationDto.Create(type, sequenceId);

			assertThat(dto.type()).isEqualTo(type);
			assertThat(dto.sequenceId()).isEqualTo(sequenceId);
		}

		@Test
		void actionConstructor() {
			final var action = new BlockAction.Create(BLOCK.getType(),
					BLOCK.getSequenceId());

			final var dto = new BlockMutationDto.Create(action);

			assertThat(dto.type()).isEqualTo(BLOCK.getTypeString());
			assertThat(dto.sequenceId()).isEqualTo(BLOCK.getSequenceId());
		}

		@Test
		void typeNull() {
			final String type = null;
			final int sequenceId = BLOCK.getSequenceId();

			assertThatThrownBy(
					() -> new BlockMutationDto.Create(type, sequenceId))
							.isInstanceOf(NullPointerException.class);
		}

		@Test
		void actionNull() {
			assertThatThrownBy(() -> new BlockMutationDto.Create(null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
