package com.cvesters.notula.topic.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.meeting.TestMeeting;
import com.cvesters.notula.organisation.TestOrganisation;
import com.cvesters.notula.topic.TestTopic;
import com.cvesters.notula.topic.bdo.TopicInfo;

class TopicDaoTest {

	private static final TestTopic TOPIC = TestTopic.SPORER_PROJECT_BLOCKERS;
	private static final TestMeeting MEETING = TOPIC.getMeeting();
	private static final TestOrganisation ORGANISATION = MEETING
			.getOrganisation();

	@Nested
	class Constructor {

		@Test
		void success() {
			final var dao = new TopicDao(TOPIC.info());

			assertThat(dao.getId()).isNull();
			assertThat(dao.getOrganisationId()).isEqualTo(ORGANISATION.getId());
			assertThat(dao.getMeetingId()).isEqualTo(MEETING.getId());
			assertThat(dao.getName()).isEqualTo(TOPIC.getName());
		}

		@Test
		void bdoNull() {
			assertThatThrownBy(() -> new TopicDao(null))
					.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class Update {
		
		private final TopicDao dao = new TopicDao(TOPIC.info());

		@Test
		void success() {
			final var updated = new TopicInfo(ORGANISATION.getId(),
					MEETING.getId(), "Updated name");

			dao.update(updated);

			assertThat(dao.getName()).isEqualTo(updated.getName());
		}

		@Test
		void bdoNull() {
			assertThatThrownBy(() -> dao.update(null))
					.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class ToBdo {

		private final TopicDao dao = new TopicDao(TOPIC.info());

		@Test
		void success() throws Exception {
			final Field idField = dao.getClass().getDeclaredField("id");
			idField.setAccessible(true);
			idField.set(dao, TOPIC.getId());

			final TopicInfo bdo = dao.toBdo();

			assertThat(bdo.getId()).isEqualTo(TOPIC.getId());
			assertThat(bdo.getOrganisationId()).isEqualTo(ORGANISATION.getId());
			assertThat(bdo.getMeetingId()).isEqualTo(MEETING.getId());
			assertThat(bdo.getName()).isEqualTo(TOPIC.getName());
		}

		@Test
		void idNull() {
			assertThatThrownBy(dao::toBdo)
					.isInstanceOf(IllegalStateException.class);
		}
	}
}
