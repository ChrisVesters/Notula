package com.cvesters.notula.details.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.details.bdo.MeetingDetails;
import com.cvesters.notula.details.bdo.TopicDetails;
import com.cvesters.notula.meeting.TestMeeting;
import com.cvesters.notula.meeting.bdo.MeetingInfo;
import com.cvesters.notula.topic.TestTopic;

class MeetingDetailsDtoTest {

	@Nested
	class Constructor {

		@Test
		void success() {
			final TestMeeting meeting = TestMeeting.GLOVER_KICKOFF_2026;
			final MeetingInfo meetingInfo = meeting.info();

			final List<TestTopic> topics = TestTopic.ofMeeting(meeting);
			final List<TopicDetails> topicsInfo = topics.stream()
					.map(TestTopic::info)
					.map(topicInfo -> new TopicDetails(topicInfo,
							Collections.emptyList()))
					.toList();
			final var details = new MeetingDetails(meetingInfo, topicsInfo);

			final var dto = new MeetingDetailsDto(details);

			assertThat(dto.getId()).isEqualTo(meetingInfo.getId());
			assertThat(dto.getName()).isEqualTo(meetingInfo.getName());

			assertThat(dto.getTopics()).hasSize(topics.size());
			topics.forEach(topic -> {
				assertThat(dto.getTopics()).anySatisfy(t -> {
					assertThat(t.getId()).isEqualTo(topic.getId());
					assertThat(t.getName()).isEqualTo(topic.getName());
					assertThat(t.getBlocks()).isEmpty(); // TODO: better check!?
				});
			});
		}

		@Test
		void detailsNull() {
			final MeetingDetails details = null;

			assertThatThrownBy(() -> new MeetingDetailsDto(details))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
