package com.cvesters.notula.topic.bdo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.common.domain.Minutes;
import com.cvesters.notula.common.domain.Rank;
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
					MEETING.getId(), TOPIC.getRank(), TOPIC.getName());

			assertThatThrownBy(result::getId)
					.isInstanceOf(IllegalStateException.class);
			assertThat(result.getOrganisationId())
					.isEqualTo(ORGANISATION.getId());
			assertThat(result.getMeetingId()).isEqualTo(MEETING.getId());
			assertThat(result.getRank()).isEqualTo(TOPIC.getRank());
			assertThat(result.getName()).isEqualTo(TOPIC.getName());
			assertThat(result.getDescription()).isEmpty();
			assertThat(result.getDuration()).isEmpty();
		}

		@Test
		void withId() {
			final long topicId = TOPIC.getId();
			final long orgId = ORGANISATION.getId();
			final long meetingId = MEETING.getId();
			final Rank rank = TOPIC.getRank();
			final String name = TOPIC.getName();
			final String description = TOPIC.getDescription();
			final var duration = TOPIC.getDuration();

			final var result = new TopicInfo(topicId, orgId, meetingId,
					rank, name, description, duration);

			assertThat(result.getId()).isEqualTo(topicId);
			assertThat(result.getOrganisationId()).isEqualTo(orgId);
			assertThat(result.getMeetingId()).isEqualTo(meetingId);
			assertThat(result.getRank()).isEqualTo(rank);
			assertThat(result.getName()).isEqualTo(name);
			assertThat(result.getDescription()).isEqualTo(description);
			assertThat(result.getDuration()).hasValue(duration);
		}

		@Test
		void rankNull() {
			final long id = TOPIC.getId();
			final long organisationId = ORGANISATION.getId();
			final long meetingId = MEETING.getId();
			final String name = TOPIC.getName();
			final String description = TOPIC.getDescription();
			final Minutes duration = TOPIC.getDuration();

			assertThatThrownBy(() -> new TopicInfo(id, organisationId,
					meetingId, null, name, description, duration))
							.isInstanceOf(NullPointerException.class);
		}

		@Test
		void nameNull() {
			final long id = TOPIC.getId();
			final long organisationId = ORGANISATION.getId();
			final long meetingId = MEETING.getId();
			final Rank rank = TOPIC.getRank();
			final String name = null;
			final String description = TOPIC.getDescription();
			final var duration = TOPIC.getDuration();

			assertThatThrownBy(() -> new TopicInfo(id, organisationId,
					meetingId, rank, name, description, duration))
							.isInstanceOf(NullPointerException.class);
		}

		@Test
		void descriptionNull() {
			final long id = TOPIC.getId();
			final long organisationId = ORGANISATION.getId();
			final long meetingId = MEETING.getId();
			final Rank rank = TOPIC.getRank();
			final String name = TOPIC.getName();
			final String description = null;
			final var duration = TOPIC.getDuration();

			assertThatThrownBy(() -> new TopicInfo(id, organisationId,
					meetingId, rank, name, description, duration))
							.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class SetRank {

		@Test
		void success() {
			final var topicInfo = TOPIC.info();
			final var rank = new Rank("z");

			topicInfo.setRank(rank);

			assertThat(topicInfo.getRank()).isEqualTo(rank);
		}

		@Test
		void rankNull() {
			final var topicInfo = TOPIC.info();

			assertThatThrownBy(() -> topicInfo.setRank(null))
					.isInstanceOf(NullPointerException.class);
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

	@Nested
	class SetDescription {

		private TopicInfo topicInfo = TOPIC.info();

		@Test
		void success() {
			final String description = "New description";

			topicInfo.setDescription(description);

			assertThat(topicInfo.getDescription()).isEqualTo(description);
		}

		@Test
		void descriptionNull() {
			assertThatThrownBy(() -> topicInfo.setDescription(null))
					.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class SetDuration {

		private TopicInfo topicInfo = TOPIC.info();

		@Test
		void success() {
			final var duration = new Minutes(45);

			topicInfo.setDuration(duration);

			assertThat(topicInfo.getDuration()).hasValue(duration);
		}

		@Test
		void durationNull() {
			topicInfo.setDuration(null);

			assertThat(topicInfo.getDuration()).isEmpty();
		}
	}
}
