package com.cvesters.notula.topic;

import java.util.List;
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

	public TopicInfo getById(final Principal principal, final long topicId) {
		Objects.requireNonNull(principal);

		final long organisationId = principal.organisationId();

		return topicStorage.find(topicId)
				.filter(t -> t.getOrganisationId() == organisationId)
				.orElseThrow(MissingEntityException::new);
	}

	public TopicInfo create(final Principal principal,
			final TopicAction.Create action) {
		Objects.requireNonNull(principal);
		Objects.requireNonNull(action);

		final MeetingInfo meeting = meetingService.getById(principal,
				action.getMeetingId());

		final List<TopicInfo> existingTopics = topicStorage
				.findAllByMeetingId(meeting.getId());
		// TODO: move logic into action?
		if (action.getSequenceId() > existingTopics.size()) {
			throw new IllegalArgumentException();
		}

		final List<TopicInfo> toUpdateTopics = existingTopics.stream()
				.filter(t -> t.getSequenceId() >= action.getSequenceId())
				.toList();
		toUpdateTopics.forEach(TopicInfo::moveDown);
		topicStorage.updateAll(toUpdateTopics);
		// TODO: publish move action/event!!

		final var topic = new TopicInfo(meeting.getOrganisationId(),
				meeting.getId(), action.getSequenceId(), action.getName());

		final TopicInfo created = topicStorage.create(topic);

		final var event = new TopicEvent(created.getId(), action);
		topicPublisher.publish(meeting.getId(), event);

		return created;
	}

	public TopicInfo update(final Principal principal, final long meetingId,
			final long topicId, final TopicAction.Update action) {
		Objects.requireNonNull(action);

		final TopicInfo topicInfo = getById(principal, topicId);
		action.apply(topicInfo);
		final TopicInfo updated = topicStorage.update(topicInfo);

		final var event = new TopicEvent(topicId, action);
		topicPublisher.publish(meetingId, event);

		return updated;
	}

	public void delete(final Principal principal, final long meetingId,
			final long topicId) {
		Objects.requireNonNull(principal);

		final TopicInfo topicInfo = getById(principal, topicId);
		topicStorage.delete(topicInfo);

		final List<TopicInfo> existingTopics = topicStorage
				.findAllByMeetingId(topicInfo.getMeetingId());
		// TODO: move logic into action?
		final List<TopicInfo> toUpdateTopics = existingTopics.stream()
				.filter(t -> t.getSequenceId() > topicInfo.getSequenceId())
				.toList();
		toUpdateTopics.forEach(TopicInfo::moveUp);
		topicStorage.updateAll(toUpdateTopics);
		// TODO: publish move action/event!!

		final var event = new TopicEvent(topicId, new TopicAction.Delete());
		topicPublisher.publish(meetingId, event);
	}

}
