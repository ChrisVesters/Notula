package com.cvesters.notula.topic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
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
	class UpdateAll {

		private static final TestMeeting MEETING = TestMeeting.SPORER_PROJECT;

		@Test
		void single() {
			final TestTopic topic = TestTopic.SPORER_PROJECT_DELIVERABLES;
			final TopicInfo updateBdo = topic.info();
			final List<TopicInfo> topics = List.of(updateBdo);

			final TopicDao found = mock();
			when(topicRepository.findByMeetingIdAndId(MEETING.getId(),
					topic.getId())).thenReturn(Optional.of(found));

			final TopicDao updatedDao = mock();
			final TopicInfo updatedBdo = mock();
			when(updatedDao.toBdo()).thenReturn(updatedBdo);

			final List<TopicDao> updatedTopics = List.of(updatedDao);
			when(topicRepository.saveAll(List.of(found)))
					.thenReturn(updatedTopics);

			final List<TopicInfo> result = gateway.updateAll(topics);

			assertThat(result).hasSize(1).contains(updatedBdo);

			final InOrder inOrder = inOrder(found, topicRepository);
			inOrder.verify(found).update(updateBdo);
			inOrder.verify(topicRepository).saveAll(List.of(found));
		}

		@Test
		void multiple() {
			final List<TestTopic> topics = List.of(
					TestTopic.SPORER_PROJECT_DELIVERABLES,
					TestTopic.SPORER_PROJECT_BLOCKERS);

			final List<TopicInfo> updateBdos = new ArrayList<>();
			final List<TopicDao> foundDaos = new ArrayList<>();
			final List<TopicDao> updatedDaos = new ArrayList<>();
			final List<TopicInfo> updatedBdos = new ArrayList<>();
			for (final TestTopic topic : topics) {
				final TopicInfo updateBdo = topic.info();
				updateBdos.add(updateBdo);

				final TopicDao found = mock();
				when(topicRepository.findByMeetingIdAndId(
						topic.getMeeting().getId(), topic.getId()))
								.thenReturn(Optional.of(found));
				foundDaos.add(found);

				final TopicDao updatedDao = mock();
				final TopicInfo updatedBdo = mock();
				when(updatedDao.toBdo()).thenReturn(updatedBdo);

				updatedDaos.add(updatedDao);
				updatedBdos.add(updatedBdo);
			}

			when(topicRepository.saveAll(foundDaos)).thenReturn(updatedDaos);

			final var result = gateway.updateAll(updateBdos);

			assertThat(result).containsExactlyElementsOf(updatedBdos);
		}

		@Test
		void emptyList() {
			final List<TopicInfo> topics = List.of();

			when(topicRepository
					.saveAll(argThat((List<TopicDao> list) -> list.isEmpty())))
							.thenReturn(List.of());

			final var result = gateway.updateAll(topics);

			assertThat(result).isEmpty();

			verify(topicRepository).saveAll(anyIterable());
			verifyNoMoreInteractions(topicRepository);
		}

		@Test
		void notFound() {
			final TestTopic topic = TestTopic.SPORER_PROJECT_DELIVERABLES;
			final List<TopicInfo> topics = List.of(topic.info());

			when(topicRepository.findByMeetingIdAndId(
					topic.getMeeting().getId(), topic.getId()))
							.thenReturn(Optional.empty());

			assertThatThrownBy(() -> gateway.updateAll(topics))
					.isInstanceOf(MissingEntityException.class);

			verify(topicRepository, never()).saveAll(anyIterable());
		}

		@Test
		void topicsNull() {
			final List<TopicInfo> topics = null;

			assertThatThrownBy(() -> gateway.updateAll(topics))
					.isInstanceOf(NullPointerException.class);
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

	@Nested
	class Delete {

		private static final TestTopic TOPIC = TestTopic.GLOVER_KICKOFF_2026_LOOKBACK;
		private static final TestMeeting MEETING = TOPIC.getMeeting();

		@Test
		void success() {
			final TopicDao topicDao = mock();
			when(topicRepository.findByMeetingIdAndId(MEETING.getId(),
					TOPIC.getId())).thenReturn(Optional.of(topicDao));

			final TopicInfo update = TOPIC.info();
			gateway.delete(update);

			final InOrder inOrder = inOrder(topicRepository);
			inOrder.verify(topicRepository).delete(topicDao);
		}

		@Test
		void notFound() {
			when(topicRepository.findByMeetingIdAndId(MEETING.getId(),
					TOPIC.getId())).thenReturn(Optional.empty());

			final TopicInfo update = TOPIC.info();
			assertThatThrownBy(() -> gateway.delete(update))
					.isInstanceOf(MissingEntityException.class);
		}

		@Test
		void topicNull() {
			assertThatThrownBy(() -> gateway.delete(null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
