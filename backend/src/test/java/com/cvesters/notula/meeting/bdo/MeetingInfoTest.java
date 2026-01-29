package com.cvesters.notula.meeting.bdo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.cvesters.notula.meeting.TestMeeting;

public class MeetingInfoTest {

	private static final TestMeeting MEETING = TestMeeting.SPORER_PROJECT;

	@Nested
	class Constructor {

		@Test
		void withoutId() {
			final var result = new MeetingInfo(
					MEETING.getOrganisation().getId(), MEETING.getName());

			assertThatThrownBy(() -> result.getId())
					.isInstanceOf(IllegalStateException.class);
			assertThat(result.getName()).isEqualTo(MEETING.getName());
		}

		@Test
		void withId() {
			final var result = new MeetingInfo(MEETING.getId(),
					MEETING.getOrganisation().getId(), MEETING.getName());

			assertThat(result.getId()).isEqualTo(MEETING.getId());
			assertThat(result.getName()).isEqualTo(MEETING.getName());
		}

		@Test
		void nameNull() {
			assertThatThrownBy(() -> new MeetingInfo(MEETING.getId(),
					MEETING.getOrganisation().getId(), null))
							.isInstanceOf(NullPointerException.class);
		}

		@ParameterizedTest
		@ValueSource(strings = { "", " " })
		void nameInvalid(final String name) {
			assertThatThrownBy(() -> new MeetingInfo(MEETING.getId(),
					MEETING.getOrganisation().getId(), name))
							.isInstanceOf(IllegalArgumentException.class);
		}
	}
}
