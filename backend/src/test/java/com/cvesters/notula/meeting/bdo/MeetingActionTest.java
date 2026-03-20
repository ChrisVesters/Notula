package com.cvesters.notula.meeting.bdo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MeetingActionTest {

	@Nested
	class Create {

		@Test
		void success() {
			final var action = new MeetingAction.Create("name");

			assertThat(action.name()).isEqualTo("name");
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
}
