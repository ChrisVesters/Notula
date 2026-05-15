package com.cvesters.notula.block.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.stream.Stream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.cvesters.notula.block.bdo.BlockType;

class BlockTypeDaoTest {

	@Nested
	class ToBdo {

		@ParameterizedTest
		@MethodSource("com.cvesters.notula.block.dao.BlockTypeDaoTest#mapping")
		void success(final BlockType type, final int id) {
			assertThat(BlockTypeDao.toBdo(id)).isEqualTo(type);
		}

		@ParameterizedTest
		@ValueSource(ints = { -1, Integer.MAX_VALUE })
		void idInvalid(final int id) {
			assertThatThrownBy(() -> BlockTypeDao.toBdo(id))
					.isInstanceOf(IllegalArgumentException.class);
		}
	}

	@Nested
	class ToDao {

		@ParameterizedTest
		@MethodSource("com.cvesters.notula.block.dao.BlockTypeDaoTest#mapping")
		void success(final BlockType type, final int id) {
			assertThat(BlockTypeDao.toDao(type)).isEqualTo(id);
		}

		void typeNull() {
			assertThatThrownBy(() -> BlockTypeDao.toDao(null))
					.isInstanceOf(NullPointerException.class);
		}
	}

	static Stream<Arguments> mapping() {
		return Stream.of(Arguments.of(BlockType.TEXT, 0));
	}
}
