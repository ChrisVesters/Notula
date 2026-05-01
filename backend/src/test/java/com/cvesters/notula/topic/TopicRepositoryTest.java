package com.cvesters.notula.topic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.context.jdbc.Sql;

import com.cvesters.notula.meeting.TestMeeting;
import com.cvesters.notula.organisation.TestOrganisation;
import com.cvesters.notula.test.RepositoryTest;
import com.cvesters.notula.topic.bdo.TopicInfo;
import com.cvesters.notula.topic.dao.TopicDao;

@Sql({ "/db/organisations.sql", "/db/meetings.sql", "/db/topics.sql" })
class TopicRepositoryTest extends RepositoryTest {

	@Autowired
	private TopicRepository topicRepository;

	@Nested
	class FindAllByMeetingId {

		@Test
		void single() {
			final TestMeeting meeting = TestMeeting.GLOVER_KICKOFF_2026;
			final List<TestTopic> topics = TestTopic.ofMeeting(meeting);

			final List<TopicDao> result = topicRepository
					.findAllByMeetingId(meeting.getId());

			assertThat(result).hasSize(1);
			topics.forEach(topic -> {
				assertThat(result).anySatisfy(t -> assertEquals(t, topic));
			});
		}

		@Test
		void multiple() {
			final TestMeeting meeting = TestMeeting.SPORER_Q2_PLANNING;
			final List<TestTopic> topics = TestTopic.ofMeeting(meeting);

			final List<TopicDao> result = topicRepository
					.findAllByMeetingId(meeting.getId());

			assertThat(result).hasSize(topics.size());
			topics.forEach(topic -> {
				assertThat(result).anySatisfy(t -> assertEquals(t, topic));
			});
		}

		@Test
		void notFound() {
			assertThat(topicRepository.findAllByMeetingId(Long.MAX_VALUE))
					.isEmpty();
		}

	}

	@Nested
	class FindByMeetingIdAndId {

		@Test
		void found() {
			final TestTopic topic = TestTopic.GLOVER_KICKOFF_2026_LOOKBACK;

			final var result = topicRepository.findByMeetingIdAndId(
					topic.getMeeting().getId(), topic.getId());

			assertThat(result)
					.hasValueSatisfying(dao -> assertEquals(dao, topic));
		}

		@Test
		void topicNotFound() {
			final TestTopic topic = TestTopic.GLOVER_KICKOFF_2026_LOOKBACK;
			final TestMeeting meeting = topic.getMeeting();

			final var result = topicRepository
					.findByMeetingIdAndId(meeting.getId(), Long.MAX_VALUE);

			assertThat(result).isEmpty();
		}

		@Test
		void meetingNotFound() {
			final TestTopic topic = TestTopic.GLOVER_KICKOFF_2026_LOOKBACK;

			final var result = topicRepository
					.findByMeetingIdAndId(Long.MAX_VALUE, topic.getId());

			assertThat(result).isEmpty();
		}
	}

	@Nested
	class Save {

		@Test
		void success() {
			final TestMeeting meeting = TestMeeting.SPORER_Q2_PLANNING;
			final TestOrganisation organisation = meeting.getOrganisation();
			final String name = "New Product Launches";
			final var bdo = new TopicInfo(organisation.getId(), meeting.getId(),
					name);
			final var dao = new TopicDao(bdo);

			final TopicDao saved = topicRepository.save(dao);

			assertThat(saved.getId()).isNotNull();
			assertThat(saved.getOrganisationId())
					.isEqualTo(organisation.getId());
			assertThat(saved.getMeetingId()).isEqualTo(meeting.getId());
			assertThat(saved.getName()).isEqualTo(name);

			final TopicDao found = entityManager.find(TopicDao.class,
					saved.getId());
			assertThat(found).isNotNull();
			assertThat(found.getId()).isEqualTo(saved.getId());
			assertThat(found.getOrganisationId())
					.isEqualTo(saved.getOrganisationId());
			assertThat(found.getMeetingId()).isEqualTo(saved.getMeetingId());
			assertThat(found.getName()).isEqualTo(saved.getName());
		}

		@Test
		void topicNull() {
			assertThatThrownBy(() -> topicRepository.save(null))
					.isInstanceOf(InvalidDataAccessApiUsageException.class);
		}
	}

	private static void assertEquals(final TopicDao dao,
			final TestTopic topic) {
		assertThat(dao.getId()).isEqualTo(topic.getId());
		assertThat(dao.getOrganisationId())
				.isEqualTo(topic.getMeeting().getOrganisation().getId());
		assertThat(dao.getMeetingId()).isEqualTo(topic.getMeeting().getId());
		assertThat(dao.getName()).isEqualTo(topic.getName());
	}
}
