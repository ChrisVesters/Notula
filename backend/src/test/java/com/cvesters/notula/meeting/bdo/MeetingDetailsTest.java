package com.cvesters.notula.meeting.bdo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.meeting.TestMeeting;
import com.cvesters.notula.topic.TestTopic;
import com.cvesters.notula.topic.bdo.TopicInfo;

class MeetingDetailsTest {

	@Nested
	class Constructor {

		private static final TestMeeting MEETING = TestMeeting.GLOVER_KICKOFF_2026;
		private static final List<TestTopic> TOPICS = TestTopic
				.ofMeeting(MEETING);

		@Test
		void success() {
			final MeetingInfo meetingInfo = MEETING.info();
			final List<TopicInfo> topicsInfo = TOPICS.stream()
					.map(TestTopic::info)
					.toList();

			final var details = new MeetingDetails(meetingInfo, topicsInfo);

			assertThat(details.info()).isEqualTo(meetingInfo);
			assertThat(details.topics()).isEqualTo(topicsInfo);
		}

		@Test
		void infoNull() {
			final MeetingInfo meetingInfo = null;
			final List<TopicInfo> topicsInfo = TOPICS.stream()
					.map(TestTopic::info)
					.toList();

			assertThatThrownBy(
					() -> new MeetingDetails(meetingInfo, topicsInfo))
							.isInstanceOf(NullPointerException.class);
		}

		@Test
		void topicsNull() {
			final MeetingInfo meetingInfo = MEETING.info();
			final List<TopicInfo> topicsInfo = null;

			assertThatThrownBy(
					() -> new MeetingDetails(meetingInfo, topicsInfo))
							.isInstanceOf(NullPointerException.class);
		}
	}
}
