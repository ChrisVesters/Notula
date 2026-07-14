package com.cvesters.notula.topic.bdo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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
					MEETING.getId(), TOPIC.getSequenceId(), TOPIC.getName());

			assertThatThrownBy(result::getId)
					.isInstanceOf(IllegalStateException.class);
			assertThat(result.getOrganisationId())
					.isEqualTo(ORGANISATION.getId());
			assertThat(result.getMeetingId()).isEqualTo(MEETING.getId());
			assertThat(result.getSequenceId()).isEqualTo(TOPIC.getSequenceId());
			assertThat(result.getName()).isEqualTo(TOPIC.getName());
			assertThat(result.getDescription()).isEmpty();
		}

		@Test
		void withId() {
			final var result = new TopicInfo(TOPIC.getId(),
					ORGANISATION.getId(), MEETING.getId(),
					TOPIC.getSequenceId(), TOPIC.getName(),
					TOPIC.getDescription());

			assertThat(result.getId()).isEqualTo(TOPIC.getId());
			assertThat(result.getOrganisationId())
					.isEqualTo(ORGANISATION.getId());
			assertThat(result.getMeetingId()).isEqualTo(MEETING.getId());
			assertThat(result.getSequenceId()).isEqualTo(TOPIC.getSequenceId());
			assertThat(result.getName()).isEqualTo(TOPIC.getName());
			assertThat(result.getDescription())
					.isEqualTo(TOPIC.getDescription());
		}

		@Test
		void sequenceIdInvalid() {
			final long id = TOPIC.getId();
			final long organisationId = ORGANISATION.getId();
			final long meetingId = MEETING.getId();
			final int sequenceId = -1;
			final String name = TOPIC.getName();
			final String description = TOPIC.getDescription();

			assertThatThrownBy(() -> new TopicInfo(id, organisationId,
					meetingId, sequenceId, name, description))
							.isInstanceOf(IllegalArgumentException.class);
		}

		@Test
		void nameNull() {
			final long id = TOPIC.getId();
			final long organisationId = ORGANISATION.getId();
			final long meetingId = MEETING.getId();
			final int sequenceId = TOPIC.getSequenceId();
			final String name = null;
			final String description = TOPIC.getDescription();

			assertThatThrownBy(() -> new TopicInfo(id, organisationId,
					meetingId, sequenceId, name, description))
							.isInstanceOf(NullPointerException.class);
		}

		@Test
		void descriptionNull() {
			final long id = TOPIC.getId();
			final long organisationId = ORGANISATION.getId();
			final long meetingId = MEETING.getId();
			final int sequenceId = TOPIC.getSequenceId();
			final String name = TOPIC.getName();
			final String description = null;

			assertThatThrownBy(() -> new TopicInfo(id, organisationId,
					meetingId, sequenceId, name, description))
							.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class MoveUp {

		@Test
		void success() {
			final TestTopic topic = TestTopic.SPORER_PROJECT_BLOCKERS;
			final var topicInfo = topic.info();

			topicInfo.moveUp();

			assertThat(topicInfo.getSequenceId())
					.isEqualTo(TOPIC.getSequenceId() - 1);
		}

		@Test
		void first() {
			final TestTopic topic = TestTopic.SPORER_PROJECT_DELIVERABLES;
			final var topicInfo = topic.info();

			assertThatThrownBy(topicInfo::moveUp)
					.isInstanceOf(IllegalStateException.class);
		}
	}

	@Nested
	class MoveDown {

		@Test
		void success() {
			final TestTopic topic = TestTopic.SPORER_PROJECT_BLOCKERS;
			final var topicInfo = topic.info();

			topicInfo.moveDown();

			assertThat(topicInfo.getSequenceId())
					.isEqualTo(topic.getSequenceId() + 1);
		}

		@Test
		void overflow() {
			final var topicInfo = new TopicInfo(TOPIC.getId(),
					ORGANISATION.getId(), MEETING.getId(), Integer.MAX_VALUE,
					TOPIC.getName(), TOPIC.getDescription());

			assertThatThrownBy(topicInfo::moveDown)
					.isInstanceOf(IllegalStateException.class);
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
}
