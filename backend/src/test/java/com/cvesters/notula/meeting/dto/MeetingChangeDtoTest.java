package com.cvesters.notula.meeting.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.common.domain.Splice;
import com.cvesters.notula.meeting.MeetingActionMatcher;
import com.cvesters.notula.meeting.bdo.MeetingAction;

class MeetingChangeDtoTest {

	private static final TextEditDto EDIT = new TextEditDto(4, 12, "Updated");
	private static final long BASE = 41L;

	@Nested
	class Rename {

		@Test
		void toBdo() {
			final var dto = new MeetingChangeDto.Rename(BASE, EDIT);

			final var rebased = new Splice(6, 12, "Updated");

			final MeetingAction.UpdateName bdo = dto.toBdo(rebased);

			final var expected = new MeetingAction.UpdateName(rebased);
			final var matcher = new MeetingActionMatcher.UpdateName(expected);
			assertThat(bdo).is(matcher.equal());
		}
	}

	@Nested
	class Describe {

		@Test
		void toBdo() {
			final var dto = new MeetingChangeDto.Describe(BASE, EDIT);

			final var rebased = new Splice(6, 12, "Updated");

			final MeetingAction.UpdateDescription bdo = dto.toBdo(rebased);

			final var expected = new MeetingAction.UpdateDescription(rebased);
			final var matcher = new MeetingActionMatcher.UpdateDescription(
					expected);
			assertThat(bdo).is(matcher.equal());
		}
	}
}
