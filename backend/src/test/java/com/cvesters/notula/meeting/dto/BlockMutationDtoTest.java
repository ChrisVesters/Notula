package com.cvesters.notula.meeting.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.block.bdo.BlockAction;
import com.cvesters.notula.block.bdo.BlockEvent;
import com.cvesters.notula.block.bdo.BlockInfo;
import com.cvesters.notula.block.bdo.BlockType;
import com.cvesters.notula.block.dto.BlockTypeDto;
import com.cvesters.notula.common.domain.Origin;
import com.cvesters.notula.common.domain.Rank;
import com.cvesters.notula.session.TestSession;

import tools.jackson.databind.ObjectMapper;

class BlockMutationDtoTest {

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
	private static final UUID CLIENT_ID = UUID
			.fromString("3f9c1a44-1d2e-4a51-8b0c-2c7e9b1d4a06");
	private static final Origin ORIGIN = new Origin(SESSION.principal(),
			CLIENT_ID);

	private static final long BLOCK_ID = 9L;

	private static final String RANK = "b2";
	private static final long TOPIC_ID = 32L;

	@Nested
	class Of {

		private static MutationDto of(final BlockAction action) {
			final BlockInfo block = mock();
			when(block.getId()).thenReturn(BLOCK_ID);
			when(block.getRank()).thenReturn(new Rank(RANK));

			return BlockMutationDto.of(new BlockEvent(block, action, ORIGIN));
		}

		@Test
		void create() {
			final var dto = of(
					new BlockAction.Create(TOPIC_ID, BlockType.TEXT, 1L));

			final var blockType = new BlockTypeDto(BlockType.TEXT);
			final var expected = new BlockMutationDto.Add(BLOCK_ID, TOPIC_ID,
					blockType, RANK);
			assertThat(dto).isEqualTo(expected);
		}

		@Test
		void move() {
			final var dto = of(new BlockAction.Move(1L));

			assertThat(dto)
					.isEqualTo(new BlockMutationDto.Move(BLOCK_ID, RANK));
		}

		@Test
		void delete() {
			final var dto = of(new BlockAction.Delete());

			assertThat(dto).isEqualTo(new BlockMutationDto.Remove(BLOCK_ID));
		}

		@Test
		void eventNull() {
			assertThatThrownBy(() -> BlockMutationDto.of(null))
					.isInstanceOf(NullPointerException.class);
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
