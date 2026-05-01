package com.cvesters.notula.topic.bdo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.cvesters.notula.meeting.TestMeeting;
import com.cvesters.notula.organisation.TestOrganisation;
import com.cvesters.notula.topic.TestTopic;

class TopicInfoTest {

	private static final TestTopic TOPIC = TestTopic.SPORER_PROJECT_BLOCKERS;
	private static final TestMeeting MEETING = TOPIC.getMeeting();
	private static final TestOrganisation ORGANISATION = MEETING
			.getOrganisation();

	@Nested
	class Constructor {

		@Test
		void withoutId() {
			final var result = new TopicInfo(ORGANISATION.getId(),
					MEETING.getId(), TOPIC.getName());

			assertThatThrownBy(result::getId)
					.isInstanceOf(IllegalStateException.class);
			assertThat(result.getOrganisationId())
					.isEqualTo(ORGANISATION.getId());
			assertThat(result.getMeetingId()).isEqualTo(MEETING.getId());
			assertThat(result.getName()).isEqualTo(TOPIC.getName());
		}

		@Test
		void withId() {
			final var result = new TopicInfo(TOPIC.getId(),
					ORGANISATION.getId(), MEETING.getId(), TOPIC.getName());

			assertThat(result.getId()).isEqualTo(TOPIC.getId());
			assertThat(result.getOrganisationId())
					.isEqualTo(ORGANISATION.getId());
			assertThat(result.getMeetingId()).isEqualTo(MEETING.getId());
			assertThat(result.getName()).isEqualTo(TOPIC.getName());
		}

		@Test
		void nameNull() {
			final long id = TOPIC.getId();
			final long organisationId = ORGANISATION.getId();
			final long meetingId = MEETING.getId();
			final String name = null;

			assertThatThrownBy(
					() -> new TopicInfo(id, organisationId, meetingId, name))
							.isInstanceOf(NullPointerException.class);
		}

		@ParameterizedTest
		@ValueSource(strings = { "", " " })
		void nameInvalid(final String name) {
			final long id = TOPIC.getId();
			final long organisationId = ORGANISATION.getId();
			final long meetingId = MEETING.getId();

			assertThatThrownBy(
					() -> new TopicInfo(id, organisationId, meetingId, name))
							.isInstanceOf(IllegalArgumentException.class);
		}
	}

	@Nested
	class SetName {

		private TopicInfo topicInfo = TOPIC.info();

		@Test
		void success() {
			final String name = "New name";

			topicInfo.setName(name);

			assertThat(topicInfo.getName()).isEqualTo(name);
		}

		@Test
		void nameNull() {
			assertThatThrownBy(() -> topicInfo.setName(null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
