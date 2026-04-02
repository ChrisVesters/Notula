package com.cvesters.notula.topic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.meeting.MeetingService;
import com.cvesters.notula.meeting.TestMeeting;
import com.cvesters.notula.organisation.TestOrganisation;
import com.cvesters.notula.session.TestSession;
import com.cvesters.notula.topic.bdo.TopicAction;
import com.cvesters.notula.topic.bdo.TopicEvent;
import com.cvesters.notula.topic.bdo.TopicInfo;

class TopicServiceTest {

	private final MeetingService meetingService = mock();

	private final TopicStorageGateway topicStorageGateway = mock();
	private final TopicPublisher topicPublisher = mock();

	private final TopicService topicService = new TopicService(meetingService,
			topicStorageGateway, topicPublisher);

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
				assertThatThrownBy(() -> t.getId())
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

			final var expectedEvent = new TopicEvent.Create(result.getId(),
					TOPIC_NAME);
			verify(topicPublisher).publish(meetingId, expectedEvent);
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
}
