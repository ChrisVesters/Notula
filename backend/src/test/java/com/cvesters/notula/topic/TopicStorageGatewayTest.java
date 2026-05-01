package com.cvesters.notula.topic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import com.cvesters.notula.common.exception.MissingEntityException;
import com.cvesters.notula.meeting.TestMeeting;
import com.cvesters.notula.topic.bdo.TopicInfo;
import com.cvesters.notula.topic.dao.TopicDao;

class TopicStorageGatewayTest {

	private final TopicRepository topicRepository = mock();

	private final TopicStorageGateway gateway = new TopicStorageGateway(
			topicRepository);

	@Nested
	class Create {

		@Test
		void success() {
			final TopicDao created = mock();
			final TopicInfo bdo = mock();
			when(created.toBdo()).thenReturn(bdo);

			when(topicRepository.save(argThat(dao -> {
				assertThat(dao.getId()).isNull();
				assertThat(dao.getOrganisationId())
						.isEqualTo(bdo.getOrganisationId());
				assertThat(dao.getMeetingId()).isEqualTo(bdo.getMeetingId());
				assertThat(dao.getName()).isEqualTo(bdo.getName());
				return true;
			}))).thenReturn(created);

			final TopicInfo topicInfo = gateway.create(bdo);

			assertThat(topicInfo).isEqualTo(bdo);
		}

		@Test
		void topicNull() {
			assertThatThrownBy(() -> gateway.create(null))
					.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class Find {

		private static final TestTopic TOPIC = TestTopic.GLOVER_KICKOFF_2026_LOOKBACK;
		private static final TestMeeting MEETING = TOPIC.getMeeting();

		@Test
		void success() {
			final long meetingId = MEETING.getId();
			final long topicId = TOPIC.getId();

			final TopicDao topicDao = mock();
			final TopicInfo topicInfo = TOPIC.info();
			when(topicDao.toBdo()).thenReturn(topicInfo);

			when(topicRepository.findByMeetingIdAndId(meetingId, topicId))
					.thenReturn(Optional.of(topicDao));

			final Optional<TopicInfo> result = gateway.find(meetingId, topicId);

			assertThat(result).contains(topicInfo);
		}

		@Test
		void notFound() {
			final long meetingId = MEETING.getId();
			final long topicId = TOPIC.getId();

			when(topicRepository.findByMeetingIdAndId(meetingId, topicId))
					.thenReturn(Optional.empty());

			final Optional<TopicInfo> result = gateway.find(meetingId, topicId);

			assertThat(result).isEmpty();
		}
	}

	@Nested
	class FindAllByMeetingId {

		@Test
		void single() {
			final long meetingId = TestMeeting.GLOVER_KICKOFF_2026.getId();
			final List<TestTopic> found = List
					.of(TestTopic.GLOVER_KICKOFF_2026_LOOKBACK);

			final var daos = new ArrayList<TopicDao>();
			final var bdos = new ArrayList<TopicInfo>();
			for (final TestTopic topic : found) {
				final TopicDao dao = mock();
				final TopicInfo bdo = topic.info();
				when(dao.toBdo()).thenReturn(bdo);

				daos.add(dao);
				bdos.add(bdo);
			}

			when(topicRepository.findAllByMeetingId(meetingId))
					.thenReturn(daos);

			final List<TopicInfo> result = gateway
					.findAllByMeetingId(meetingId);

			assertThat(result).isEqualTo(bdos);
		}

		@Test
		void multiple() {
			final long meetingId = TestMeeting.SPORER_Q2_PLANNING.getId();
			final List<TestTopic> found = TestTopic
					.ofMeeting(TestMeeting.SPORER_Q2_PLANNING);

			final var daos = new ArrayList<TopicDao>();
			final var bdos = new ArrayList<TopicInfo>();
			for (final TestTopic topic : found) {
				final TopicDao dao = mock();
				final TopicInfo bdo = topic.info();
				when(dao.toBdo()).thenReturn(bdo);

				daos.add(dao);
				bdos.add(bdo);
			}

			when(topicRepository.findAllByMeetingId(meetingId))
					.thenReturn(daos);

			final List<TopicInfo> result = gateway
					.findAllByMeetingId(meetingId);

			assertThat(result).isEqualTo(bdos);
		}

		@Test
		void notFound() {
			when(topicRepository.findAllByMeetingId(Long.MAX_VALUE))
					.thenReturn(Collections.emptyList());

			assertThat(gateway.findAllByMeetingId(Long.MAX_VALUE)).isEmpty();
		}
	}

	@Nested
	class Update {

		private static final TestTopic TOPIC = TestTopic.GLOVER_KICKOFF_2026_LOOKBACK;
		private static final TestMeeting MEETING = TOPIC.getMeeting();

		@Test
		void success() {
			final long meetingId = MEETING.getId();
			final long topicId = TOPIC.getId();

			final TopicDao topicDao = mock();

			when(topicRepository.findByMeetingIdAndId(meetingId, topicId))
					.thenReturn(Optional.of(topicDao));

			final TopicDao updatedDao = mock();
			final TopicInfo updatedBdo = mock();
			when(updatedDao.toBdo()).thenReturn(updatedBdo);

			when(topicRepository.save(topicDao)).thenReturn(updatedDao);

			final TopicInfo update = TOPIC.info();

			final TopicInfo result = gateway.update(update);

			assertThat(result).isEqualTo(updatedBdo);

			final InOrder inOrder = inOrder(topicDao, topicRepository);
			inOrder.verify(topicDao).update(update);
			inOrder.verify(topicRepository).save(topicDao);
		}

		@Test
		void notFound() {
			final long meetingId = MEETING.getId();
			final long topicId = TOPIC.getId();

			when(topicRepository.findByMeetingIdAndId(meetingId, topicId))
					.thenReturn(Optional.empty());

			final TopicInfo update = TOPIC.info();

			assertThatThrownBy(() -> gateway.update(update))
					.isInstanceOf(MissingEntityException.class);
		}

		@Test
		void topicNull() {
			assertThatThrownBy(() -> gateway.update(null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
