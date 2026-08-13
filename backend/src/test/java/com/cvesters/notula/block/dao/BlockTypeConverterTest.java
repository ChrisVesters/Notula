package com.cvesters.notula.block.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.cvesters.notula.block.bdo.BlockType;

class BlockTypeConverterTest {

	public final BlockTypeConverter converter = new BlockTypeConverter();

	@Nested
	class ConvertToDatabaseColumn {

		@Test
		void text() {
			assertThat(converter.convertToDatabaseColumn(BlockType.TEXT))
					.isZero();
		}

		@Test
		void roleNull() {
			assertThatThrownBy(() -> converter.convertToDatabaseColumn(null))
					.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class ConvertToEntityAttribute {

		@Test
		void admin() {
			assertThat(converter.convertToEntityAttribute((short) 0))
					.isEqualTo(BlockType.TEXT);
		}

		@Test
		void valueNull() {
			assertThatThrownBy(() -> converter.convertToEntityAttribute(null))
					.isInstanceOf(NullPointerException.class);
		}

		@ParameterizedTest
		@ValueSource(ints = { -1, 10 })
		void valueInvalid(final int value) {
			assertThatThrownBy(
					() -> converter.convertToEntityAttribute((short) value))
							.isInstanceOf(IllegalArgumentException.class);
		}
	}
}
