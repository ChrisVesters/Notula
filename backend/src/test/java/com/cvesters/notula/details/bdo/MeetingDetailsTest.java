package com.cvesters.notula.details.bdo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.meeting.TestMeeting;
import com.cvesters.notula.meeting.bdo.MeetingInfo;
import com.cvesters.notula.topic.TestTopic;

class MeetingDetailsTest {

	@Nested
	class Constructor {

		private static final TestMeeting MEETING = TestMeeting.GLOVER_KICKOFF_2026;
		private static final List<TestTopic> TOPICS = TestTopic
				.ofMeeting(MEETING);

		@Test
		void success() {
			final MeetingInfo meetingInfo = MEETING.info();
			final List<TopicDetails> topicsDetails = TOPICS.stream()
					.map(TestTopic::info)
					.map(info -> new TopicDetails(info,
							Collections.emptyList()))
					.toList();

			final var details = new MeetingDetails(meetingInfo, topicsDetails);

			assertThat(details.getId()).isEqualTo(MEETING.getId());
			assertThat(details.getName()).isEqualTo(MEETING.getName());
			assertThat(details.getTopics()).isEqualTo(topicsDetails);
		}

		@Test
		void infoNull() {
			final MeetingInfo meetingInfo = null;
			final List<TopicDetails> topicsDetails = TOPICS.stream()
					.map(TestTopic::info)
					.map(info -> new TopicDetails(info,
							Collections.emptyList()))
					.toList();

			assertThatThrownBy(
					() -> new MeetingDetails(meetingInfo, topicsDetails))
							.isInstanceOf(NullPointerException.class);
		}

		@Test
		void topicsNull() {
			final MeetingInfo meetingInfo = MEETING.info();
			final List<TopicDetails> topicsInfo = null;

			assertThatThrownBy(
					() -> new MeetingDetails(meetingInfo, topicsInfo))
							.isInstanceOf(NullPointerException.class);
		}
	}
}
