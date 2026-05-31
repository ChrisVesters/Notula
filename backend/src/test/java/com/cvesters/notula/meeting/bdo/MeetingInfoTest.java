package com.cvesters.notula.meeting.bdo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.cvesters.notula.meeting.TestMeeting;
import com.cvesters.notula.organisation.TestOrganisation;

public class MeetingInfoTest {

	private static final TestMeeting MEETING = TestMeeting.SPORER_PROJECT;
	private static final TestOrganisation ORGANISATION = MEETING
			.getOrganisation();

	@Nested
	class Constructor {

		@Test
		void withoutId() {
			final var result = new MeetingInfo(ORGANISATION.getId(),
					MEETING.getName());

			assertThatThrownBy(result::getId)
					.isInstanceOf(IllegalStateException.class);
			assertThat(result.getOrganisationId())
					.isEqualTo(ORGANISATION.getId());
			assertThat(result.getName()).isEqualTo(MEETING.getName());
			assertThat(result.getDescription()).isEmpty();
		}

		@Test
		void withId() {
			final var result = new MeetingInfo(MEETING.getId(),
					ORGANISATION.getId(), MEETING.getName(),
					MEETING.getDescription());

			assertThat(result.getId()).isEqualTo(MEETING.getId());
			assertThat(result.getOrganisationId())
					.isEqualTo(ORGANISATION.getId());
			assertThat(result.getName()).isEqualTo(MEETING.getName());
			assertThat(result.getDescription())
					.isEqualTo(MEETING.getDescription());
		}

		@Test
		void nameNull() {
			final long meetingId = MEETING.getId();
			final long organisationId = ORGANISATION.getId();
			final String name = null;
			final String description = MEETING.getDescription();

			assertThatThrownBy(() -> new MeetingInfo(meetingId, organisationId,
					name, description))
							.isInstanceOf(NullPointerException.class);
		}

		@Test
		void descriptionNull() {
			final long meetingId = MEETING.getId();
			final long organisationId = ORGANISATION.getId();
			final String name = MEETING.getName();
			final String description = null;

			assertThatThrownBy(() -> new MeetingInfo(meetingId, organisationId,
					name, description))
							.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class SetName {

		private final MeetingInfo info = MEETING.info();

		@ParameterizedTest
		@ValueSource(strings = { "Upated", "!@#$%^&*(){}[]|\\:;\"'<>,.?/",
				"Встреча: 你好 مرحبا" })
		void success(final String name) {
			info.setName(name);

			assertThat(info.getName()).isEqualTo(name);
		}

		@Test
		void nameNull() {
			assertThatThrownBy(() -> info.setName(null))
					.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class SetDescription {

		private final MeetingInfo info = MEETING.info();

		@ParameterizedTest
		@ValueSource(strings = { "Upated", "!@#$%^&*(){}[]|\\:;\"'<>,.?/",
				"Встреча: 你好 مرحبا" })
		void success(final String description) {
			info.setDescription(description);

			assertThat(info.getDescription()).isEqualTo(description);
		}

		@Test
		void descriptionNull() {
			assertThatThrownBy(() -> info.setDescription(null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
