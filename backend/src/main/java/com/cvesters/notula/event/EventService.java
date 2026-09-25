package com.cvesters.notula.event;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.common.exception.MissingEntityException;
import com.cvesters.notula.common.messaging.TransactionalPublisher;
import com.cvesters.notula.event.bdo.EventInfo;
import com.cvesters.notula.event.dto.EventDto;
import com.cvesters.notula.meeting.MeetingStorageGateway;

@Service
public class EventService {

	private static final String TOPIC = "/topic/meetings/";

	private final TransactionalPublisher publisher;
	private final EventStorageGateway eventStorage;
	private final MeetingStorageGateway meetingStorage;

	public EventService(final TransactionalPublisher publisher,
			final EventStorageGateway eventStorage,
			final MeetingStorageGateway meetingStorage) {
		this.publisher = publisher;
		this.eventStorage = eventStorage;
		this.meetingStorage = meetingStorage;
	}

	public List<EventInfo> findAllSince(final Principal principal,
			final long meetingId, final long revision) {
		Objects.requireNonNull(principal);

		final long organisationId = principal.organisationId();
		meetingStorage.find(meetingId)
				.filter(m -> m.getOrganisationId() == organisationId)
				.orElseThrow(MissingEntityException::new);

		return eventStorage.findAllSince(meetingId, revision);
	}

	public void publish(final EventInfo event) {
		Objects.requireNonNull(event);

		final EventInfo created = eventStorage.create(event);

		final String destination = TOPIC + created.getMeetingId();
		publisher.send(destination, new EventDto(created));
	}
}
