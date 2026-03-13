package com.cvesters.notula.topic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.meeting.TestMeeting;
import com.cvesters.notula.meeting.bdo.MeetingInfo;
import com.cvesters.notula.organisation.TestOrganisation;
import com.cvesters.notula.session.TestSession;
import com.cvesters.notula.topic.bdo.TopicAction;
import com.cvesters.notula.topic.bdo.TopicInfo;

class TopicServiceTest {

	private final TopicStorageGateway topicStorageGateway = mock();

	private final TopicService topicService = new TopicService(
			topicStorageGateway);

	@Nested
	class Create {

		private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
		private static final TestMeeting MEETING = TestMeeting.SPORER_PROJECT;
		private static final TestOrganisation ORGANISATION = MEETING
				.getOrganisation();

		@Test
		void success() {
			final MeetingInfo meetingInfo = MEETING.info();
			final TopicAction.Create topic = new TopicAction.Create("Topic");

			final var topicInfo = new TopicInfo(ORGANISATION.getId(),
					MEETING.getId(), "Topic");

			when(topicStorageGateway.create(argThat(t -> {
				assertThatThrownBy(() -> t.getId())
						.isInstanceOf(IllegalStateException.class);
				assertThat(t.getOrganisationId())
						.isEqualTo(ORGANISATION.getId());
				assertThat(t.getMeetingId()).isEqualTo(MEETING.getId());
				assertThat(t.getName()).isEqualTo("Topic");
				return true;
			}))).thenReturn(topicInfo);

			final TopicInfo result = topicService.create(meetingInfo, topic);

			assertThat(result).isEqualTo(topicInfo);
		}

		@Test
		void meetingNull() {
			final MeetingInfo meetingInfo = null;
			final TopicAction.Create topic = new TopicAction.Create("Topic");

			assertThatThrownBy(() -> topicService.create(meetingInfo, topic))
					.isInstanceOf(NullPointerException.class);
		}

		@Test
		void topicNull() {
			final MeetingInfo meetingInfo = MEETING.info();
			final TopicAction.Create topic = null;

			assertThatThrownBy(() -> topicService.create(meetingInfo, topic))
					.isInstanceOf(NullPointerException.class);
		}

	}

	@Nested
	class GetAllByMeetingId {

		private static final TestMeeting MEETING = TestMeeting.SPORER_PROJECT;

		@ParameterizedTest
		@MethodSource("cases")
		void success(final List<TestTopic> topics) {
			final Principal principal = TestSession.EDUARDO_CHRISTIANSEN_SPORER
					.principal();

			final List<TopicInfo> info = topics.stream()
					.map(TestTopic::info)
					.toList();

			when(topicStorageGateway.findAllByMeetingId(MEETING.getId()))
					.thenReturn(info);

			final List<TopicInfo> result = topicService
					.getAllForMeeting(MEETING.info());

			assertThat(result).isEqualTo(info);
		}

		@Test
		void meetingNull() {
			assertThatThrownBy(() -> topicService.getAllForMeeting(null))
					.isInstanceOf(NullPointerException.class);
		}

		private static List<List<TestTopic>> cases() {
			return List.of(List.of(), List.of(TestTopic.SPORER_PROJECT_BLOCKERS,
					TestTopic.SPORER_PROJECT_DELIVERABLES));
		}
	}
}
