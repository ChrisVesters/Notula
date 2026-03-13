package com.cvesters.notula.topic;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.cvesters.notula.meeting.bdo.MeetingInfo;
import com.cvesters.notula.topic.bdo.TopicAction;
import com.cvesters.notula.topic.bdo.TopicInfo;

@Service
public class TopicService {

	private final TopicStorageGateway topicStorage;

	public TopicService(final TopicStorageGateway topicStorage) {
		this.topicStorage = topicStorage;
	}

	public TopicInfo create(final MeetingInfo meeting,
			final TopicAction.Create topic) {
		Objects.requireNonNull(meeting);
		Objects.requireNonNull(topic);

		final var topicInfo = new TopicInfo(meeting.getOrganisationId(),
				meeting.getId(), topic.name());

		return topicStorage.create(topicInfo);
	}

	public List<TopicInfo> getAllForMeeting(final MeetingInfo meeting) {
		Objects.requireNonNull(meeting);

		return topicStorage.findAllByMeetingId(meeting.getId());
	}
}
