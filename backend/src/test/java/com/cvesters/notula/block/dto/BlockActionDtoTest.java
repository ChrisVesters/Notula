package com.cvesters.notula.block.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.block.TestBlock;
import com.cvesters.notula.block.bdo.BlockAction;
import com.cvesters.notula.meeting.TestMeeting;
import com.cvesters.notula.topic.TestTopic;

class BlockActionDtoTest {

	private static final TestBlock BLOCK = TestBlock.SPORER_PROJECT_BLOCKERS_FIRST;
	private static final TestTopic TOPIC = BLOCK.getTopic();
	private static final TestMeeting MEETING = TOPIC.getMeeting();

	@Nested
	class Create {

		@Test
		void toBdo() {
			final var dto = new BlockActionDto.Create(MEETING.getId(),
					TOPIC.getId(), BLOCK.getTypeDto(),
					BLOCK.getSequenceId());
			final BlockAction.Create bdo = dto.toBdo();

			assertThat(bdo.getTopicId()).isEqualTo(TOPIC.getId());
			assertThat(bdo.getType()).isEqualTo(BLOCK.getType());
			assertThat(bdo.getSequenceId()).isEqualTo(BLOCK.getSequenceId());
		}
	}
}
