package com.cvesters.notula.topic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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

		private static final long TOPIC_ID = Long.MAX_VALUE;
		private static final String TOPIC_NAME = "Topic";

		private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
		private static final Principal PRINCIPAL = SESSION.principal();
		private static final TestMeeting MEETING = TestMeeting.SPORER_PROJECT;
		private static final TestOrganisation ORGANISATION = MEETING
				.getOrganisation();

		@Test
		void success() {
			final long meetingId = MEETING.getId();
			final TopicAction.Create topic = new TopicAction.Create(TOPIC_NAME);

			final var created = new TopicInfo(TOPIC_ID, ORGANISATION.getId(),
					MEETING.getId(), TOPIC_NAME);

			when(meetingService.getById(PRINCIPAL, meetingId))
					.thenReturn(MEETING.info());

			when(topicStorageGateway.create(argThat(t -> {
				assertThatThrownBy(t::getId)
						.isInstanceOf(IllegalStateException.class);
				assertThat(t.getOrganisationId())
						.isEqualTo(ORGANISATION.getId());
				assertThat(t.getMeetingId()).isEqualTo(meetingId);
				assertThat(t.getName()).isEqualTo(TOPIC_NAME);
				return true;
			}))).thenReturn(created);

			final TopicInfo result = topicService.create(PRINCIPAL, meetingId,
					topic);

			assertThat(result).isEqualTo(created);

			final var expectedAction = new TopicAction.Create(TOPIC_NAME);
			final var matcher = new TopicActionMatcher.Create(expectedAction);
			verify(topicPublisher).publish(eq(meetingId), argThat(event -> {
				assertThat(event.topicId()).isEqualTo(TOPIC_ID);
				assertThat(event.action()).is(matcher.equal());
				return true;
			}));
		}

		@Test
		void principalNull() {
			final long meetingId = MEETING.getId();
			final TopicAction.Create topic = new TopicAction.Create("Topic");

			assertThatThrownBy(
					() -> topicService.create(null, meetingId, topic))
							.isInstanceOf(NullPointerException.class);
		}

		@Test
		void actionNull() {
			final long meetingId = MEETING.getId();

			assertThatThrownBy(
					() -> topicService.create(PRINCIPAL, meetingId, null))
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
				assertThat(info.getName()).isEqualTo("Project Timeline");
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
}
