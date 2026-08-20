package com.cvesters.notula.topic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.common.exception.MissingEntityException;
import com.cvesters.notula.meeting.MeetingService;
import com.cvesters.notula.meeting.TestMeeting;
import com.cvesters.notula.meeting.bdo.MeetingInfo;
import com.cvesters.notula.organisation.TestOrganisation;
import com.cvesters.notula.session.TestSession;
import com.cvesters.notula.topic.bdo.TopicAction;
import com.cvesters.notula.topic.bdo.TopicInfo;

class TopicServiceTest {

	private final MeetingService meetingService = mock();

	private final TopicStorageGateway topicStorageGateway = mock();
	private final TopicPublisher topicPublisher = mock();

	private final TopicService topicService = new TopicService(meetingService,
			topicStorageGateway, topicPublisher);

	@Nested
	class GetById {

		private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
		private static final Principal PRINCIPAL = SESSION.principal();
		private static final TestTopic TOPIC = TestTopic.SPORER_PROJECT_TIMELINE;
		private static final TestMeeting MEETING = TOPIC.getMeeting();

		@Test
		void success() {
			final MeetingInfo meetingInfo = MEETING.info();
			when(meetingService.getById(PRINCIPAL, MEETING.getId()))
					.thenReturn(meetingInfo);
			when(meetingService.getById(PRINCIPAL, MEETING.getId()))
					.thenReturn(meetingInfo);

			final TopicInfo topicInfo = TOPIC.info();
			when(topicStorageGateway.find(MEETING.getId(), TOPIC.getId()))
					.thenReturn(Optional.of(topicInfo));

			final TopicInfo result = topicService.getById(PRINCIPAL,
					MEETING.getId(), TOPIC.getId());

			assertThat(result).isEqualTo(topicInfo);
		}

		@Test
		void notFound() {
			final long meetingId = MEETING.getId();
			final long topicId = TOPIC.getId();

			final MeetingInfo meetingInfo = MEETING.info();
			when(meetingService.getById(PRINCIPAL, meetingId))
					.thenReturn(meetingInfo);

			when(topicStorageGateway.find(meetingId, topicId))
					.thenReturn(Optional.empty());

			assertThatThrownBy(
					() -> topicService.getById(PRINCIPAL, meetingId, topicId))
							.isInstanceOf(MissingEntityException.class);
		}

		@Test
		void principalNull() {
			final long meetingId = MEETING.getId();
			final long topicId = TOPIC.getId();

			assertThatThrownBy(
					() -> topicService.getById(null, meetingId, topicId))
							.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class Create {
		private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
		private static final Principal PRINCIPAL = SESSION.principal();
		private static final TestMeeting MEETING = TestMeeting.SPORER_PROJECT;
		private static final TestOrganisation ORGANISATION = MEETING
				.getOrganisation();

		private static final long MEETING_ID = MEETING.getId();
		private static final long TOPIC_ID = Long.MAX_VALUE;
		private static final int TOPIC_SEQUENCE_ID = 0;
		private static final String TOPIC_NAME = "Topic";
		private static final String TOPIC_DESCRIPTION = "Description";

		@Test
		void firstTopic() {
			when(meetingService.getById(PRINCIPAL, MEETING_ID))
					.thenReturn(MEETING.info());

			when(topicStorageGateway.findAllByMeetingId(MEETING_ID))
					.thenReturn(Collections.emptyList());

			final var created = new TopicInfo(TOPIC_ID, ORGANISATION.getId(),
					MEETING.getId(), TOPIC_SEQUENCE_ID, TOPIC_NAME,
					TOPIC_DESCRIPTION, null);

			when(topicStorageGateway.create(argThat(t -> {
				assertThatThrownBy(t::getId)
						.isInstanceOf(IllegalStateException.class);
				assertThat(t.getOrganisationId())
						.isEqualTo(ORGANISATION.getId());
				assertThat(t.getMeetingId()).isEqualTo(MEETING_ID);
				assertThat(t.getSequenceId()).isEqualTo(TOPIC_SEQUENCE_ID);
				assertThat(t.getName()).isEqualTo(TOPIC_NAME);
				assertThat(t.getDescription()).isEmpty();
				assertThat(t.getDuration()).isEmpty();
				return true;
			}))).thenReturn(created);

			final var action = new TopicAction.Create(MEETING.getId(),
					TOPIC_SEQUENCE_ID, TOPIC_NAME);

			final TopicInfo result = topicService.create(PRINCIPAL, action);

			assertThat(result).isEqualTo(created);

			final var expectedAction = new TopicAction.Create(MEETING.getId(),
					TOPIC_SEQUENCE_ID, TOPIC_NAME);
			final var matcher = new TopicActionMatcher.Create(expectedAction);
			verify(topicPublisher).publish(eq(MEETING_ID), argThat(event -> {
				assertThat(event.topicId()).isEqualTo(TOPIC_ID);
				assertThat(event.action()).is(matcher.equal());
				return true;
			}));
		}

		@Test
		void topicAtEnd() {
			when(meetingService.getById(PRINCIPAL, MEETING_ID))
					.thenReturn(MEETING.info());

			final List<TopicInfo> existingTopics = TestTopic.ofMeeting(MEETING)
					.stream()
					.map(TestTopic::info)
					.toList();
			when(topicStorageGateway.findAllByMeetingId(MEETING_ID))
					.thenReturn(existingTopics);

			final int sequenceId = existingTopics.size();
			final var created = new TopicInfo(TOPIC_ID, ORGANISATION.getId(),
					MEETING.getId(), sequenceId, TOPIC_NAME, TOPIC_DESCRIPTION,
					null);

			when(topicStorageGateway.create(argThat(t -> {
				assertThatThrownBy(t::getId)
						.isInstanceOf(IllegalStateException.class);
				assertThat(t.getOrganisationId())
						.isEqualTo(ORGANISATION.getId());
				assertThat(t.getMeetingId()).isEqualTo(MEETING_ID);
				assertThat(t.getSequenceId()).isEqualTo(sequenceId);
				assertThat(t.getName()).isEqualTo(TOPIC_NAME);
				assertThat(t.getDescription()).isEmpty();
				assertThat(t.getDuration()).isEmpty();
				return true;
			}))).thenReturn(created);

			final var action = new TopicAction.Create(MEETING.getId(),
					sequenceId, TOPIC_NAME);

			final TopicInfo result = topicService.create(PRINCIPAL, action);

			assertThat(result).isEqualTo(created);

			final var expectedAction = new TopicAction.Create(MEETING.getId(),
					sequenceId, TOPIC_NAME);
			final var matcher = new TopicActionMatcher.Create(expectedAction);
			verify(topicPublisher).publish(eq(MEETING_ID), argThat(event -> {
				assertThat(event.topicId()).isEqualTo(TOPIC_ID);
				assertThat(event.action()).is(matcher.equal());
				return true;
			}));

			final InOrder inOrder = inOrder(topicStorageGateway);
			inOrder.verify(topicStorageGateway)
					.updateAll(Collections.emptyList());
			inOrder.verify(topicStorageGateway).create(any());
		}

		@Test
		void topicAtStart() {
			when(meetingService.getById(PRINCIPAL, MEETING_ID))
					.thenReturn(MEETING.info());

			final List<TopicInfo> existingTopics = TestTopic.ofMeeting(MEETING)
					.stream()
					.map(TestTopic::info)
					.toList();
			when(topicStorageGateway.findAllByMeetingId(MEETING_ID))
					.thenReturn(existingTopics);

			final int sequenceId = 0;
			final var created = new TopicInfo(TOPIC_ID, ORGANISATION.getId(),
					MEETING.getId(), sequenceId, TOPIC_NAME, TOPIC_DESCRIPTION,
					null);

			when(topicStorageGateway.create(argThat(t -> {
				assertThatThrownBy(t::getId)
						.isInstanceOf(IllegalStateException.class);
				assertThat(t.getOrganisationId())
						.isEqualTo(ORGANISATION.getId());
				assertThat(t.getMeetingId()).isEqualTo(MEETING_ID);
				assertThat(t.getSequenceId()).isZero();
				assertThat(t.getName()).isEqualTo(TOPIC_NAME);
				assertThat(t.getDescription()).isEmpty();
				assertThat(t.getDuration()).isEmpty();
				return true;
			}))).thenReturn(created);

			final var action = new TopicAction.Create(MEETING.getId(),
					TOPIC_SEQUENCE_ID, TOPIC_NAME);

			final TopicInfo result = topicService.create(PRINCIPAL, action);

			assertThat(result).isEqualTo(created);

			final var expectedAction = new TopicAction.Create(MEETING_ID,
					TOPIC_SEQUENCE_ID, TOPIC_NAME);
			final var matcher = new TopicActionMatcher.Create(expectedAction);
			verify(topicPublisher).publish(eq(MEETING_ID), argThat(event -> {
				assertThat(event.topicId()).isEqualTo(TOPIC_ID);
				assertThat(event.action()).is(matcher.equal());
				return true;
			}));

			final InOrder inOrder = inOrder(topicStorageGateway);
			// TODO: should have been moved down!
			inOrder.verify(topicStorageGateway).updateAll(existingTopics);
			inOrder.verify(topicStorageGateway).create(any());
		}

		@Test
		void invalidSequenceId() {
			when(meetingService.getById(PRINCIPAL, MEETING_ID))
					.thenReturn(MEETING.info());

			when(topicStorageGateway.findAllByMeetingId(MEETING_ID))
					.thenReturn(Collections.emptyList());

			final var action = new TopicAction.Create(MEETING_ID, 1, TOPIC_NAME);

			assertThatThrownBy(() -> topicService.create(PRINCIPAL, action))
					.isInstanceOf(IllegalArgumentException.class);

			verifyNoInteractions(topicPublisher);
			verify(topicStorageGateway, never()).updateAll(any());
			verify(topicStorageGateway, never()).create(any());
		}

		@Test
		void principalNull() {
			final TopicAction.Create topic = new TopicAction.Create(MEETING_ID,
					TOPIC_SEQUENCE_ID, TOPIC_NAME);

			assertThatThrownBy(() -> topicService.create(null, topic))
					.isInstanceOf(NullPointerException.class);
		}

		@Test
		void actionNull() {
			assertThatThrownBy(() -> topicService.create(PRINCIPAL, null))
					.isInstanceOf(NullPointerException.class);
		}

	}

	@Nested
	class Update {

		private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
		private static final TestTopic TOPIC = TestTopic.SPORER_PROJECT_TIMELINE;
		private static final TestMeeting MEETING = TOPIC.getMeeting();

		@Test
		void success() {
			final Principal principal = SESSION.principal();
			final long meetingId = MEETING.getId();
			final long topicId = TOPIC.getId();

			final MeetingInfo meetingInfo = MEETING.info();
			when(meetingService.getById(principal, meetingId))
					.thenReturn(meetingInfo);

			final TopicInfo topicInfo = TOPIC.info();
			when(topicStorageGateway.find(meetingId, topicId))
					.thenReturn(Optional.of(topicInfo));

			final TopicInfo updated = mock();
			when(topicStorageGateway.update(argThat(info -> {
				assertThat(info.getId()).isEqualTo(topicId);
				assertThat(info.getOrganisationId())
						.isEqualTo(MEETING.getOrganisation().getId());
				assertThat(info.getMeetingId()).isEqualTo(meetingId);
				assertThat(info.getSequenceId())
						.isEqualTo(TOPIC.getSequenceId());
				assertThat(info.getName()).isEqualTo("Project Timeline");
				assertThat(info.getDescription())
						.isEqualTo(TOPIC.getDescription());
				return true;
			}))).thenReturn(updated);

			final TopicAction.Update action = new TopicAction.UpdateName(0, 0,
					"Project ");
			final TopicInfo result = topicService.update(principal, meetingId,
					topicId, action);

			assertThat(result).isEqualTo(updated);

			verify(topicPublisher).publish(eq(meetingId), argThat(event -> {
				assertThat(event.topicId()).isEqualTo(topicId);
				assertThat(event.action()).isEqualTo(action);
				return true;
			}));
		}

		@Test
		void notFound() {
			final Principal principal = SESSION.principal();
			final long meetingId = MEETING.getId();
			final long topicId = TOPIC.getId();

			final MeetingInfo meetingInfo = MEETING.info();
			when(meetingService.getById(principal, meetingId))
					.thenReturn(meetingInfo);

			when(topicStorageGateway.find(meetingId, topicId))
					.thenReturn(Optional.empty());

			final TopicAction.Update action = new TopicAction.UpdateName(0, 0,
					"Project ");

			assertThatThrownBy(() -> topicService.update(principal, meetingId,
					topicId, action))
							.isInstanceOf(MissingEntityException.class);

			verify(topicStorageGateway, never()).update(any());
			verifyNoInteractions(topicPublisher);
		}

		@Test
		void principalNull() {
			final long meetingId = MEETING.getId();
			final long topicId = TOPIC.getId();

			final TopicAction.Update action = new TopicAction.UpdateName(0, 0,
					"Project ");

			assertThatThrownBy(
					() -> topicService.update(null, meetingId, topicId, action))
							.isInstanceOf(NullPointerException.class);
		}

		@Test
		void actionNull() {
			final Principal principal = SESSION.principal();
			final long meetingId = MEETING.getId();
			final long topicId = TOPIC.getId();

			assertThatThrownBy(() -> topicService.update(principal, meetingId,
					topicId, null)).isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class Delete {

		private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
		private static final TestTopic TOPIC = TestTopic.SPORER_PROJECT_TIMELINE;
		private static final TestMeeting MEETING = TOPIC.getMeeting();

		@Test
		void onlyTopic() {
			final Principal principal = SESSION.principal();
			final long meetingId = MEETING.getId();
			final long topicId = TOPIC.getId();

			final MeetingInfo meetingInfo = MEETING.info();
			when(meetingService.getById(principal, meetingId))
					.thenReturn(meetingInfo);

			final TopicInfo topicInfo = TOPIC.info();
			when(topicStorageGateway.find(meetingId, topicId))
					.thenReturn(Optional.of(topicInfo));

			when(topicStorageGateway.findAllByMeetingId(meetingId))
					.thenReturn(List.of(topicInfo));

			topicService.delete(principal, meetingId, topicId);

			verify(topicStorageGateway).delete(topicInfo);
			verify(topicPublisher).publish(eq(meetingId), argThat(event -> {
				assertThat(event.topicId()).isEqualTo(topicId);
				assertThat(event.action())
						.isInstanceOf(TopicAction.Delete.class);
				return true;
			}));

			verify(topicStorageGateway).updateAll(Collections.emptyList());
		}

		@Test
		void firstTopic() {
			final TestMeeting meeting = TestMeeting.SPORER_PROJECT;
			final List<TestTopic> topics = TestTopic.ofMeeting(MEETING);
			final TestTopic deleted = topics.getFirst();

			final Principal principal = SESSION.principal();
			final long meetingId = meeting.getId();
			final long topicId = deleted.getId();

			final MeetingInfo meetingInfo = MEETING.info();
			when(meetingService.getById(principal, meetingId))
					.thenReturn(meetingInfo);

			final TopicInfo topicInfo = deleted.info();
			when(topicStorageGateway.find(meetingId, topicId))
					.thenReturn(Optional.of(topicInfo));

			final List<TopicInfo> existingTopics = topics.stream()
					.map(TestTopic::info)
					.toList();
			when(topicStorageGateway.findAllByMeetingId(meetingId))
					.thenReturn(existingTopics);

			topicService.delete(principal, meetingId, topicId);

			verify(topicStorageGateway).delete(topicInfo);
			verify(topicPublisher).publish(eq(meetingId), argThat(event -> {
				assertThat(event.topicId()).isEqualTo(topicId);
				assertThat(event.action())
						.isInstanceOf(TopicAction.Delete.class);
				return true;
			}));

			// TODO: should have been moved up!
			final List<TopicInfo> toUpdateTopics = existingTopics.stream()
					.filter(t -> t.getId() != topicId)
					.toList();
			verify(topicStorageGateway).updateAll(toUpdateTopics);
		}

		@Test
		void lastTopic() {
			final TestMeeting meeting = TestMeeting.SPORER_PROJECT;
			final List<TestTopic> topics = TestTopic.ofMeeting(MEETING);
			final TestTopic deleted = topics.getLast();

			final Principal principal = SESSION.principal();
			final long meetingId = meeting.getId();
			final long topicId = deleted.getId();

			final MeetingInfo meetingInfo = MEETING.info();
			when(meetingService.getById(principal, meetingId))
					.thenReturn(meetingInfo);

			final TopicInfo topicInfo = deleted.info();
			when(topicStorageGateway.find(meetingId, topicId))
					.thenReturn(Optional.of(topicInfo));

			final List<TopicInfo> existingTopics = topics.stream()
					.map(TestTopic::info)
					.toList();
			when(topicStorageGateway.findAllByMeetingId(meetingId))
					.thenReturn(existingTopics);

			topicService.delete(principal, meetingId, topicId);

			verify(topicStorageGateway).delete(topicInfo);
			verify(topicPublisher).publish(eq(meetingId), argThat(event -> {
				assertThat(event.topicId()).isEqualTo(topicId);
				assertThat(event.action())
						.isInstanceOf(TopicAction.Delete.class);
				return true;
			}));

			verify(topicStorageGateway).updateAll(Collections.emptyList());
		}

		@Test
		void notFound() {
			final Principal principal = SESSION.principal();
			final long meetingId = MEETING.getId();
			final long topicId = TOPIC.getId();

			final MeetingInfo meetingInfo = MEETING.info();
			when(meetingService.getById(principal, meetingId))
					.thenReturn(meetingInfo);

			when(topicStorageGateway.find(meetingId, topicId))
					.thenReturn(Optional.empty());

			assertThatThrownBy(
					() -> topicService.delete(principal, meetingId, topicId))
							.isInstanceOf(MissingEntityException.class);

			verify(topicStorageGateway, never()).delete(any());
			verifyNoInteractions(topicPublisher);
		}

		@Test
		void principalNull() {
			final long meetingId = MEETING.getId();
			final long topicId = TOPIC.getId();

			assertThatThrownBy(
					() -> topicService.delete(null, meetingId, topicId))
							.isInstanceOf(NullPointerException.class);
		}
	}
}
