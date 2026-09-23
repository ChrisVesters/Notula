package com.cvesters.notula.meeting.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.block.BlockActionMatcher;
import com.cvesters.notula.block.TestBlock;
import com.cvesters.notula.block.bdo.BlockAction;
import com.cvesters.notula.topic.TestTopic;

class BlockChangeDtoTest {

	private static final Long AFTER_ID = 7L;

	private static final TestBlock BLOCK = TestBlock.SPORER_PROJECT_BLOCKERS_FIRST;
	private static final TestTopic TOPIC = BLOCK.getTopic();

	@Nested
	class Add {

		@Test
		void toBdo() {
			final var dto = new BlockChangeDto.Add(TOPIC.getId(),
					BLOCK.getType(), AFTER_ID);

			final BlockAction.Create bdo = dto.toBdo();

			final var expected = new BlockAction.Create(TOPIC.getId(),
					BLOCK.getType(), AFTER_ID);
			final var matcher = new BlockActionMatcher.Create(expected);
			assertThat(bdo).is(matcher.equal());
		}
	}

	@Nested
	class Move {

		@Test
		void toBdo() {
			final var dto = new BlockChangeDto.Move(BLOCK.getId(), AFTER_ID);

			final BlockAction.Move bdo = dto.toBdo();

			final var matcher = new BlockActionMatcher.Move(
					new BlockAction.Move(AFTER_ID));
			assertThat(bdo).is(matcher.equal());
		}
	}
}
