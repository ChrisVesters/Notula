package com.cvesters.notula.details.bdo;

import java.util.List;
import java.util.Objects;

import lombok.Getter;

import com.cvesters.notula.meeting.bdo.MeetingInfo;

@Getter
public class MeetingDetails {

	private final long id;
	private final String name;
	private final String description;

	private final List<TopicDetails> topics;

	public MeetingDetails(final MeetingInfo info,
			final List<TopicDetails> topics) {
		Objects.requireNonNull(info);
		Objects.requireNonNull(topics);

		this.id = info.getId();
		this.name = info.getName();
		this.description = info.getDescription();

		this.topics = List.copyOf(topics);
	}

}
