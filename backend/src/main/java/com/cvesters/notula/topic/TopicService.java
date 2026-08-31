package com.cvesters.notula.topic;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cvesters.notula.common.domain.Origin;
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

	@Transactional
	public TopicInfo create(final Origin origin,
			final TopicAction.Create action) {
		Objects.requireNonNull(origin);
		Objects.requireNonNull(action);

		final MeetingInfo meeting = meetingService.getById(origin.principal(),
				action.getMeetingId());

		final List<TopicInfo> existingTopics = topicStorage
				.findAllByMeetingId(meeting.getId());
		if (action.getSequenceId() > existingTopics.size()) {
			throw new IllegalArgumentException();
		}

		final var events = new ArrayList<TopicEvent>();
		for (final TopicInfo t : existingTopics) {
			if (t.getSequenceId() < action.getSequenceId()) {
				continue;
			}

			final int updatedSequenceId = t.getSequenceId() + 1;
			final var move = new TopicAction.Move(updatedSequenceId);
			move.apply(t);
			final TopicInfo updatedTopic = topicStorage.update(t);
			events.add(new TopicEvent(updatedTopic, move, origin));
		}

		final var topic = new TopicInfo(meeting.getOrganisationId(),
				meeting.getId(), action.getSequenceId(), action.getName());
		final TopicInfo created = topicStorage.create(topic);
		events.add(new TopicEvent(created, action, origin));

		events.forEach(topicPublisher::publish);

		return created;
	}

	@Transactional
	public TopicInfo move(final Origin origin, final long topicId,
			final TopicAction.Move action) {
		Objects.requireNonNull(origin);
		Objects.requireNonNull(action);

		final TopicInfo topic = getById(origin.principal(), topicId);
		final int from = topic.getSequenceId();
		final int to = action.getSequenceId();
		final int direction = Integer.signum(to - from);
		if (direction == 0) {
			return topic;
		}

		final List<TopicInfo> existingTopics = topicStorage
				.findAllByMeetingId(topic.getMeetingId());
		if (to >= existingTopics.size()) {
			throw new IllegalArgumentException();
		}

		final int min = Math.min(from + direction, to);
		final int max = Math.max(from + direction, to);
		final List<TopicInfo> toUpdateTopics = existingTopics.stream()
				.filter(t -> t.getSequenceId() >= min)
				.filter(t -> t.getSequenceId() <= max)
				.toList();

		final var events = new ArrayList<TopicEvent>();

		action.apply(topic);
		final TopicInfo updated = topicStorage.update(topic);
		events.add(new TopicEvent(updated, action, origin));

		for (final TopicInfo t : toUpdateTopics) {
			final int updatedSequenceId = t.getSequenceId() - direction;
			final var move = new TopicAction.Move(updatedSequenceId);
			move.apply(t);
			final TopicInfo updatedTopic = topicStorage.update(t);
			events.add(new TopicEvent(updatedTopic, move, origin));
		}

		events.forEach(topicPublisher::publish);

		return topic;
	}

	public TopicInfo update(final Origin origin, final long topicId,
			final TopicAction.Update action) {
		Objects.requireNonNull(origin);
		Objects.requireNonNull(action);

		final TopicInfo topicInfo = getById(origin.principal(), topicId);
		action.apply(topicInfo);
		final TopicInfo updated = topicStorage.update(topicInfo);

		final var event = new TopicEvent(updated, action, origin);
		topicPublisher.publish(event);

		return updated;
	}

	@Transactional
	public void delete(final Origin origin, final long topicId) {
		Objects.requireNonNull(origin);

		final TopicInfo topicInfo = getById(origin.principal(), topicId);
		topicStorage.delete(topicInfo);

		final var events = new ArrayList<TopicEvent>();
		events.add(new TopicEvent(topicInfo, new TopicAction.Delete(), origin));

		final List<TopicInfo> existingTopics = topicStorage
				.findAllByMeetingId(topicInfo.getMeetingId());
		for (final TopicInfo t : existingTopics) {
			if (t.getSequenceId() <= topicInfo.getSequenceId()) {
				continue;
			}

			final int updatedSequenceId = t.getSequenceId() - 1;
			final var move = new TopicAction.Move(updatedSequenceId);
			move.apply(t);
			final TopicInfo updatedTopic = topicStorage.update(t);
			events.add(new TopicEvent(updatedTopic, move, origin));
		}

		events.forEach(topicPublisher::publish);
	}

}
