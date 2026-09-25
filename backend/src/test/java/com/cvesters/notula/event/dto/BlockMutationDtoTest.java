package com.cvesters.notula.event.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.block.bdo.BlockType;
import com.cvesters.notula.block.dto.BlockTypeDto;
import com.cvesters.notula.common.domain.Rank;
import com.cvesters.notula.event.bdo.BlockMutation;

import tools.jackson.databind.ObjectMapper;

class BlockMutationDtoTest {

	private static final long BLOCK_ID = 9L;

	private static final String RANK = "b2";
	private static final long TOPIC_ID = 32L;

	@Nested
	class Of {

		@Test
		void add() {
			final var mutation = new BlockMutation.Add(BLOCK_ID, TOPIC_ID,
					BlockType.TEXT, new Rank(RANK));

			final var dto = BlockMutationDto.of(mutation);

			final var blockType = new BlockTypeDto(BlockType.TEXT);
			final var expected = new BlockMutationDto.Add(BLOCK_ID, TOPIC_ID,
					blockType, RANK);
			assertThat(dto).isEqualTo(expected);
		}

		@Test
		void move() {
			final var mutation = new BlockMutation.Move(BLOCK_ID,
					new Rank(RANK));

			final var dto = BlockMutationDto.of(mutation);

			assertThat(dto)
					.isEqualTo(new BlockMutationDto.Move(BLOCK_ID, RANK));
		}

		@Test
		void remove() {
			final var mutation = new BlockMutation.Remove(BLOCK_ID);

			final var dto = BlockMutationDto.of(mutation);

			assertThat(dto).isEqualTo(new BlockMutationDto.Remove(BLOCK_ID));
		}

		@Test
		void mutationNull() {
			assertThatThrownBy(() -> BlockMutationDto.of(null))
					.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class ToBdo {

		@Test
		void add() {
			final var blockType = new BlockTypeDto(BlockType.TEXT);
			final var dto = new BlockMutationDto.Add(BLOCK_ID, TOPIC_ID,
					blockType, RANK);

			final var expected = new BlockMutation.Add(BLOCK_ID, TOPIC_ID,
					BlockType.TEXT, new Rank(RANK));
			assertThat(dto.toBdo()).isEqualTo(expected);
		}

		@Test
		void move() {
			final var dto = new BlockMutationDto.Move(BLOCK_ID, RANK);

			final var expected = new BlockMutation.Move(BLOCK_ID,
					new Rank(RANK));
			assertThat(dto.toBdo()).isEqualTo(expected);
		}

		@Test
		void remove() {
			final var dto = new BlockMutationDto.Remove(BLOCK_ID);

			final var expected = new BlockMutation.Remove(BLOCK_ID);
			assertThat(dto.toBdo()).isEqualTo(expected);
		}
	}

	@Nested
	class Serialise {

		private static final ObjectMapper MAPPER = new ObjectMapper();

		private static String write(final MutationDto mutation) {
			return MAPPER.writeValueAsString(mutation);
		}

		@Test
		void add() {
			final var blockType = new BlockTypeDto(BlockType.TEXT);
			final String json = write(new BlockMutationDto.Add(BLOCK_ID,
					TOPIC_ID, blockType, RANK));

			assertThat(json).isEqualToIgnoringWhitespace("""
					{
						"type": "ADD_BLOCK",
						"block": 9,
						"topic": 32,
						"blockType": "TEXT",
						"rank": "b2"
					}
					""");
		}

		@Test
		void move() {
			final String json = write(
					new BlockMutationDto.Move(BLOCK_ID, RANK));

			assertThat(json).isEqualToIgnoringWhitespace("""
					{
						"type": "MOVE_BLOCK",
						"block": 9,
						"rank": "b2"
					}
					""");
		}

		@Test
		void remove() {
			final String json = write(new BlockMutationDto.Remove(BLOCK_ID));

			assertThat(json).isEqualToIgnoringWhitespace("""
					{
						"type": "REMOVE_BLOCK",
						"block": 9
					}
					""");
		}
	}
}
