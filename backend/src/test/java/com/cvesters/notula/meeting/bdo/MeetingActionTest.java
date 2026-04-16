package com.cvesters.notula.meeting.bdo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.cvesters.notula.meeting.TestMeeting;

class MeetingActionTest {

	@Nested
	class Create {

		@Test
		void success() {
			final var action = new MeetingAction.Create("name");

			assertThat(action.getName()).isEqualTo("name");
		}

		@Test
		void nameNull() {
			assertThatThrownBy(() -> new MeetingAction.Create(null))
					.isInstanceOf(NullPointerException.class);
		}

		@ParameterizedTest
		@ValueSource(strings = { "", " " })
		void nameInvalid(final String name) {
			assertThatThrownBy(() -> new MeetingAction.Create(name))
					.isInstanceOf(IllegalArgumentException.class);
		}
	}

	@Nested
	class UpdateName {

		@Test
		void success() {
			final var action = new MeetingAction.UpdateName(0, 4, "test");

			assertThat(action).isNotNull();
		}

		@Test
		void offsetNegative() {
			assertThatThrownBy(
					() -> new MeetingAction.UpdateName(-1, 4, "test"))
							.isInstanceOf(IllegalArgumentException.class);
		}

		@Test
		void lengthNegative() {
			assertThatThrownBy(
					() -> new MeetingAction.UpdateName(0, -1, "test"))
							.isInstanceOf(IllegalArgumentException.class);
		}

		@Test
		void valueNull() {
			assertThatThrownBy(() -> new MeetingAction.UpdateName(0, 4, null))
					.isInstanceOf(NullPointerException.class);
		}

		@ParameterizedTest
		@CsvSource({ "0,0,'Great ','Great 2026 Kickoff Meeting'",
				"5,0,'Great ','2026 Great Kickoff Meeting'",
				"20,0,' Great','2026 Kickoff Meeting Great'",
				"0,5,'','Kickoff Meeting'", "5,8,'','2026 Meeting'",
				"12,8,'','2026 Kickoff'", "0,4,'2027','2027 Kickoff Meeting'",
				"5,7,'Test','2026 Test Meeting'",
				"13,7,'Session','2026 Kickoff Session'", "0,20,'X','X'" })
		void apply(final int position, final int length, final String value,
				final String expected) {
			final var action = new MeetingAction.UpdateName(position, length,
					value);
			final var meeting = TestMeeting.GLOVER_KICKOFF_2026.info();
			action.apply(meeting);

			assertThat(meeting.getName()).isEqualTo(expected);
		}

		@ParameterizedTest
		@CsvSource({ "21,0", "0,21", "5,16" })
		void outOfBounds(final int position, final int length) {
			final var action = new MeetingAction.UpdateName(position, length,
					"X");
			final var meeting = TestMeeting.GLOVER_KICKOFF_2026.info();

			assertThatThrownBy(() -> action.apply(meeting))
					.isInstanceOf(IllegalArgumentException.class);
		}
	}
}
