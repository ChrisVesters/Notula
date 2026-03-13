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

			assertThatThrownBy(() -> result.getId())
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
			assertThatThrownBy(() -> new TopicInfo(TOPIC.getId(),
					ORGANISATION.getId(), MEETING.getId(), null))
							.isInstanceOf(NullPointerException.class);
		}

		@ParameterizedTest
		@ValueSource(strings = { "", " " })
		void nameInvalid(final String name) {
			assertThatThrownBy(() -> new TopicInfo(TOPIC.getId(),
					ORGANISATION.getId(), MEETING.getId(), name))
							.isInstanceOf(IllegalArgumentException.class);
		}
	}
}
