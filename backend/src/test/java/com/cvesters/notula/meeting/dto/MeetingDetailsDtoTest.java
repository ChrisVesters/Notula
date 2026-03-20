package com.cvesters.notula.meeting.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.meeting.TestMeeting;
import com.cvesters.notula.meeting.bdo.MeetingDetails;
import com.cvesters.notula.meeting.bdo.MeetingInfo;
import com.cvesters.notula.topic.TestTopic;
import com.cvesters.notula.topic.bdo.TopicInfo;

class MeetingDetailsDtoTest {

	@Nested
	class Constructor {

		@Test
		void success() {
			final TestMeeting meeting = TestMeeting.GLOVER_KICKOFF_2026;
			final List<TestTopic> topics = TestTopic.ofMeeting(meeting);
			final MeetingInfo info = meeting.info();
			final List<TopicInfo> topicsInfo = topics.stream()
					.map(TestTopic::info)
					.toList();
			final var details = new MeetingDetails(info, topicsInfo);

			final var dto = new MeetingDetailsDto(details);

			assertThat(dto.info().id()).isEqualTo(info.getId());
			assertThat(dto.info().name()).isEqualTo(info.getName());
			assertThat(dto.topics()).hasSize(topics.size());
			topics.forEach(topic -> {
				assertThat(dto.topics()).anySatisfy(t -> {
					assertThat(t.id()).isEqualTo(topic.getId());
					assertThat(t.name()).isEqualTo(topic.getName());
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
