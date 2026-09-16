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
			assertThat(result.getRevision()).isZero();
		}

		@Test
		void withId() {
			final var result = new MeetingInfo(MEETING.getId(),
					ORGANISATION.getId(), MEETING.getName(),
					MEETING.getDescription(), MEETING.getRevision());

			assertThat(result.getId()).isEqualTo(MEETING.getId());
			assertThat(result.getOrganisationId())
					.isEqualTo(ORGANISATION.getId());
			assertThat(result.getName()).isEqualTo(MEETING.getName());
			assertThat(result.getDescription())
					.isEqualTo(MEETING.getDescription());
			assertThat(result.getRevision()).isEqualTo(MEETING.getRevision());
		}

		@Test
		void nameNull() {
			final long meetingId = MEETING.getId();
			final long organisationId = ORGANISATION.getId();
			final String name = null;
			final String description = MEETING.getDescription();
			final long revision = MEETING.getRevision();

			assertThatThrownBy(() -> new MeetingInfo(meetingId, organisationId,
					name, description, revision))
							.isInstanceOf(NullPointerException.class);
		}

		@Test
		void descriptionNull() {
			final long meetingId = MEETING.getId();
			final long organisationId = ORGANISATION.getId();
			final String name = MEETING.getName();
			final String description = null;
			final long revision = MEETING.getRevision();

			assertThatThrownBy(() -> new MeetingInfo(meetingId, organisationId,
					name, description, revision))
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

	@Nested
	class BumpRevision {

		@Test
		void success() {
			final MeetingInfo meeting = MEETING.info();

			meeting.bumpRevision();

			assertThat(meeting.getRevision())
					.isEqualTo(MEETING.getRevision() + 1);
		}

		@Test
		void repeated() {
			final MeetingInfo meeting = MEETING.info();

			meeting.bumpRevision();
			meeting.bumpRevision();

			assertThat(meeting.getRevision())
					.isEqualTo(MEETING.getRevision() + 2);
		}
	}
}
