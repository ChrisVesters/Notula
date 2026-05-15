package com.cvesters.notula.block.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.stream.Stream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.cvesters.notula.block.bdo.BlockType;

class BlockTypeDtoTest {

	@Nested
	class ToBdo {

		@ParameterizedTest
		@MethodSource("com.cvesters.notula.block.dto.BlockTypeDtoTest#mapping")
		void success(final BlockType type, final String value) {
			assertThat(BlockTypeDto.toBdo(value)).isEqualTo(type);
		}

		@Test
		void typeNull() {
			assertThatThrownBy(() -> BlockTypeDto.toBdo(null))
					.isInstanceOf(NullPointerException.class);
		}

		@ParameterizedTest
		@ValueSource(strings = { "", " ", "UNKNOWN" })
		void valueInvalid(final String value) {
			assertThatThrownBy(() -> BlockTypeDto.toBdo(value))
					.isInstanceOf(IllegalArgumentException.class);
		}
	}

	@Nested
	class ToDto {

		@ParameterizedTest
		@MethodSource("com.cvesters.notula.block.dto.BlockTypeDtoTest#mapping")
		void success(final BlockType type, final String value) {
			assertThat(BlockTypeDto.toDto(type)).isEqualTo(value);
		}

		void typeNull() {
			assertThatThrownBy(() -> BlockTypeDto.toDto(null))
					.isInstanceOf(NullPointerException.class);
		}
	}

	static Stream<Arguments> mapping() {
		return Stream.of(Arguments.of(BlockType.TEXT, "TEXT"));
	}
}
