package com.cvesters.notula.meeting.dto;

import java.util.List;
import java.util.Objects;

import com.cvesters.notula.meeting.bdo.MeetingDetails;
import com.cvesters.notula.topic.dto.TopicInfoDto;

public record MeetingDetailsDto(MeetingInfoDto info,
		List<TopicInfoDto> topics) {

	public MeetingDetailsDto(final MeetingDetails details) {
		Objects.requireNonNull(details);

		final MeetingInfoDto info = new MeetingInfoDto(details.info());
		final List<TopicInfoDto> topics = details.topics()
				.stream()
				.map(TopicInfoDto::new)
				.toList();

		this(info, topics);
	}
}
