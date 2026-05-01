package com.cvesters.notula.topic;

import java.util.Objects;

import org.springframework.stereotype.Service;

import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.common.exception.MissingEntityException;
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

	public TopicInfo getById(final Principal principal, final long meetingId,
			final long topicId) {
		Objects.requireNonNull(principal);

		final MeetingInfo meeting = meetingService.getById(principal,
				meetingId);

		return topicStorage.find(meeting.getId(), topicId)
				.orElseThrow(MissingEntityException::new);
	}

	public TopicInfo create(final Principal principal, final long meetingId,
			final TopicAction.Create action) {
		Objects.requireNonNull(principal);
		Objects.requireNonNull(action);

		final MeetingInfo meeting = meetingService.getById(principal,
				meetingId);
		final var topic = new TopicInfo(meeting.getOrganisationId(),
				meeting.getId(), action.getName());

		final TopicInfo created = topicStorage.create(topic);

		final var event = new TopicEvent(created.getId(), action);
		topicPublisher.publish(meetingId, event);

		return created;
	}

	public TopicInfo update(final Principal principal, final long meetingId,
			final long topicId, final TopicAction.Update action) {
		Objects.requireNonNull(action);

		final TopicInfo topicInfo = getById(principal, meetingId, topicId);
		action.apply(topicInfo);
		final TopicInfo updated = topicStorage.update(topicInfo);

		final var event = new TopicEvent(topicId, action);
		topicPublisher.publish(meetingId, event);

		return updated;
	}

}
