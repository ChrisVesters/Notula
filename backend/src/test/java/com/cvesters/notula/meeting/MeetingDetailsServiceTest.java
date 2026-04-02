package com.cvesters.notula.meeting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.common.exception.MissingEntityException;
import com.cvesters.notula.meeting.bdo.MeetingDetails;
import com.cvesters.notula.meeting.bdo.MeetingInfo;
import com.cvesters.notula.session.TestSession;
import com.cvesters.notula.topic.TestTopic;
import com.cvesters.notula.topic.TopicStorageGateway;
import com.cvesters.notula.topic.bdo.TopicInfo;

class MeetingDetailsServiceTest {

	private final MeetingStorageGateway meetingStorage = mock();
	private final TopicStorageGateway topicStorage = mock();

	private final MeetingDetailsService meetingActionService = new MeetingDetailsService(
			meetingStorage, topicStorage);

	@Nested
	class Get {

		private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
		private static final Principal PRINCIPAL = SESSION.principal();
		private static final TestMeeting MEETING = TestMeeting.SPORER_PROJECT;
		private static final List<TestTopic> TOPICS = TestTopic
				.ofMeeting(MEETING);

		@Test
		void success() {
			final MeetingInfo info = MEETING.info();
			when(meetingStorage.findByOrganisationIdAndId(
					PRINCIPAL.organisationId(), MEETING.getId()))
							.thenReturn(Optional.of(info));

			final List<TopicInfo> topicsInfo = TOPICS.stream()
					.map(TestTopic::info)
					.toList();
			when(topicStorage.findAllByMeetingId(MEETING.getId()))
					.thenReturn(topicsInfo);

			final MeetingDetails result = meetingActionService.get(PRINCIPAL,
					MEETING.getId());

			assertThat(result).isNotNull();
			assertThat(result.info()).isEqualTo(info);
			assertThat(result.topics()).isEqualTo(topicsInfo);
		}

		@Test
		void notFound() {
			when(meetingStorage.findByOrganisationIdAndId(
					PRINCIPAL.organisationId(), MEETING.getId()))
							.thenReturn(Optional.empty());

			assertThatThrownBy(
					() -> meetingActionService.get(PRINCIPAL, MEETING.getId()))
							.isInstanceOf(MissingEntityException.class);
		}

		@Test
		void principalNull() {
			assertThatThrownBy(
					() -> meetingActionService.get(null, MEETING.getId()))
							.isInstanceOf(NullPointerException.class);
		}

		@Test
		void principalWithoutOrganisation() {
			final Principal principal = TestSession.ALISON_DACH.principal();
			assertThatThrownBy(
					() -> meetingActionService.get(principal, MEETING.getId()))
							.isInstanceOf(IllegalStateException.class);
		}
	}

}
