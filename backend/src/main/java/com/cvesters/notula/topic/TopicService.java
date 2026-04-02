package com.cvesters.notula.topic;

import java.util.Objects;

import org.springframework.stereotype.Service;

import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.meeting.MeetingService;
import com.cvesters.notula.meeting.bdo.MeetingInfo;
import com.cvesters.notula.topic.bdo.TopicAction;
import com.cvesters.notula.topic.bdo.TopicEvent;
import com.cvesters.notula.topic.bdo.TopicInfo;

@Service
public class TopicService {

	private final MeetingService meetingService;

	private final TopicStorageGateway topicStorage;
	private final TopicPublisher topicPublisher;

	public TopicService(final MeetingService meetingService,
			final TopicStorageGateway topicStorage,
			final TopicPublisher topicPublisher) {
		this.meetingService = meetingService;
		this.topicStorage = topicStorage;
		this.topicPublisher = topicPublisher;
	}

	public TopicInfo create(final Principal principal, final long meetingId,
			final TopicAction.Create action) {
		Objects.requireNonNull(principal);
		Objects.requireNonNull(action);

		final MeetingInfo meeting = meetingService.getById(principal,
				meetingId);
		final var topic = new TopicInfo(meeting.getOrganisationId(),
				meeting.getId(), action.name());

		final TopicInfo created = topicStorage.create(topic);

		final TopicEvent event = new TopicEvent.Create(created);
		topicPublisher.publish(meetingId, event);

		return created;
	}

}
