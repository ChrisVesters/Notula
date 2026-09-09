package com.cvesters.notula.meeting.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.meeting.MeetingActionMatcher;
import com.cvesters.notula.meeting.bdo.MeetingAction;

class MeetingChangeDtoTest {

	private static final TextEditDto EDIT = new TextEditDto(4, 12, "Updated");

	@Nested
	class Rename {

		@Test
		void toBdo() {
			final var dto = new MeetingChangeDto.Rename(EDIT);

			final MeetingAction.UpdateName bdo = dto.toBdo();

			final var expected = new MeetingAction.UpdateName(4, 12,
					"Updated");
			final var matcher = new MeetingActionMatcher.UpdateName(expected);
			assertThat(bdo).is(matcher.equal());
		}
	}

	@Nested
	class Describe {

		@Test
		void toBdo() {
			final var dto = new MeetingChangeDto.Describe(EDIT);

			final MeetingAction.UpdateDescription bdo = dto.toBdo();

			final var expected = new MeetingAction.UpdateDescription(4, 12,
					"Updated");
			final var matcher = new MeetingActionMatcher.UpdateDescription(
					expected);
			assertThat(bdo).is(matcher.equal());
		}
	}
}
