package com.cvesters.notula.textblock.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.block.TestBlock;
import com.cvesters.notula.meeting.TestMeeting;
import com.cvesters.notula.textblock.TextBlockActionMatcher;
import com.cvesters.notula.textblock.bdo.TextBlockAction;
import com.cvesters.notula.topic.TestTopic;

class TextBlockActionDtoTest {

	private static final TestBlock BLOCK = TestBlock.SPORER_PROJECT_BLOCKERS_FIRST;
	private static final TestTopic TOPIC = BLOCK.getTopic();
	private static final TestMeeting MEETING = TOPIC.getMeeting();

	@Nested
	class UpdateContent {

		@Test
		void toBdo() {
			final var dto = new TextBlockActionDto.Update.Content(
					MEETING.getId(), TOPIC.getId(), BLOCK.getId(), 5, 2,
					"Updated");
			final TextBlockAction.Update bdo = dto.toBdo();

			final var expected = new TextBlockAction.UpdateContent(5, 2,
					"Updated");
			final var matcher = new TextBlockActionMatcher.UpdateContent(
					expected);
			assertThat(bdo).is(matcher.equal());
		}
	}
}
