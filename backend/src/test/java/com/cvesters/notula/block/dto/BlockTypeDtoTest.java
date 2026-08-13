package com.cvesters.notula.block.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.block.bdo.BlockType;
import com.cvesters.notula.common.exception.InvalidActionException;

class BlockTypeDtoTest {

	@Nested
	class Constructor {

		@Test
		void text() {
			final var dto = new BlockTypeDto(BlockType.TEXT);

			assertThat(dto.type()).isEqualTo("TEXT");
		}

		@Test
		void typeNull() {
			final BlockType type = null;

			assertThatThrownBy(() -> new BlockTypeDto(type))
					.isInstanceOf(NullPointerException.class);
		}

		@Test
		void valueNull() {
			final String value = null;

			assertThatThrownBy(() -> new BlockTypeDto(value))
					.isInstanceOf(NullPointerException.class);
		}

		@Test
		void valueInvalid() {
			final var dto = new BlockTypeDto("invalid");

			assertThat(dto.type()).isEqualTo("invalid");
		}
	}

	@Nested
	class ToBdo {

		@Test
		void enumConstructed() {
			final var dto = new BlockTypeDto(BlockType.TEXT);

			assertThat(dto.toBdo()).isEqualTo(BlockType.TEXT);
		}

		@Test
		void text() {
			final var dto = new BlockTypeDto("TEXT");

			assertThat(dto.toBdo()).isEqualTo(BlockType.TEXT);
		}

		@Test
		void typeInvalid() {
			final var dto = new BlockTypeDto("invalid");

			assertThatThrownBy(dto::toBdo)
					.isInstanceOf(InvalidActionException.class);
		}
	}
}
