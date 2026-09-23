package com.cvesters.notula.topic;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.cvesters.notula.common.domain.Origin;
import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.common.domain.Rank;
import com.cvesters.notula.common.exception.MissingEntityException;
import com.cvesters.notula.meeting.EventPublisher;
import com.cvesters.notula.meeting.MeetingService;
import com.cvesters.notula.meeting.bdo.MeetingInfo;
import com.cvesters.notula.meeting.bdo.MeetingScope;
import com.cvesters.notula.topic.bdo.TopicAction;
import com.cvesters.notula.topic.bdo.TopicEvent;
import com.cvesters.notula.topic.bdo.TopicInfo;

@Service
public class TopicService {

	private final MeetingService meetingService;

	private final TopicStorageGateway topicStorage;
	private final EventPublisher eventPublisher;

	public TopicService(final MeetingService meetingService,
			final TopicStorageGateway topicStorage,
			final EventPublisher eventPublisher) {
		this.meetingService = meetingService;
		this.topicStorage = topicStorage;
		this.eventPublisher = eventPublisher;
	}

	public TopicInfo getById(final Principal principal, final long topicId) {
		Objects.requireNonNull(principal);

		final long organisationId = principal.organisationId();

		return topicStorage.find(topicId)
				.filter(t -> t.getOrganisationId() == organisationId)
				.orElseThrow(MissingEntityException::new);
	}

	public TopicInfo getById(final Principal principal, final long meetingId,
			final long topicId) {
		final TopicInfo topic = getById(principal, topicId);
		if (topic.getMeetingId() != meetingId) {
			throw new MissingEntityException();
		}

		return topic;
	}

	public TopicInfo create(final Origin origin, final MeetingScope scope,
			final TopicAction.Create action) {
		Objects.requireNonNull(origin);
		Objects.requireNonNull(scope);
		Objects.requireNonNull(action);

		final MeetingInfo meeting = meetingService.getById(origin.principal(),
				scope.meetingId());

		final List<TopicInfo> siblings = topicStorage
				.findAllByMeetingId(meeting.getId());

		final TopicInfo after = action.getAfterId()
				.map(id -> find(siblings, id))
				.orElse(null);

		final Rank rank = Rank.after(after, siblings, TopicInfo::getRank);

		final var topic = new TopicInfo(meeting.getOrganisationId(),
				meeting.getId(), rank, action.getName());
		final TopicInfo created = topicStorage.create(topic);

		eventPublisher.publish(scope, new TopicEvent(created, action, origin));

		return created;
	}

	public TopicInfo move(final Origin origin, final MeetingScope scope,
			final long topicId, final TopicAction.Move action) {
		Objects.requireNonNull(origin);
		Objects.requireNonNull(scope);
		Objects.requireNonNull(action);

		final TopicInfo topic = getById(origin.principal(), scope.meetingId(),
				topicId);

		final List<TopicInfo> siblings = topicStorage
				.findAllByMeetingId(topic.getMeetingId())
				.stream()
				.filter(t -> t.getId() != topicId)
				.toList();

		final TopicInfo after = action.getAfterId()
				.map(id -> find(siblings, id))
				.orElse(null);

		final Rank rank = Rank.after(after, siblings, TopicInfo::getRank);
		topic.setRank(rank);

		final TopicInfo updated = topicStorage.update(topic);

		eventPublisher.publish(scope, new TopicEvent(updated, action, origin));

		return updated;
	}

	public TopicInfo update(final Origin origin, final MeetingScope scope,
			final long topicId, final TopicAction.Update action) {
		Objects.requireNonNull(origin);
		Objects.requireNonNull(scope);
		Objects.requireNonNull(action);

		final TopicInfo topicInfo = getById(origin.principal(),
				scope.meetingId(), topicId);
		action.apply(topicInfo);
		final TopicInfo updated = topicStorage.update(topicInfo);

		final var event = new TopicEvent(updated, action, origin);
		eventPublisher.publish(scope, event);

		return updated;
	}

	public void delete(final Origin origin, final MeetingScope scope,
			final long topicId) {
		Objects.requireNonNull(origin);
		Objects.requireNonNull(scope);

		final TopicInfo topic = getById(origin.principal(), scope.meetingId(),
				topicId);
		topicStorage.delete(topic);

		eventPublisher.publish(scope,
				new TopicEvent(topic, new TopicAction.Delete(), origin));
	}

	private static TopicInfo find(final List<TopicInfo> elements,
			final long topicId) {
		return elements.stream()
				.filter(b -> b.getId() == topicId)
				.findFirst()
				.orElseThrow(MissingEntityException::new);
	}
}
