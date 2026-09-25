package com.cvesters.notula.event;

import java.util.Objects;

import org.springframework.stereotype.Service;

import com.cvesters.notula.common.messaging.TransactionalPublisher;
import com.cvesters.notula.event.bdo.EventInfo;
import com.cvesters.notula.event.dto.EventDto;

@Service
public class EventService {

	private static final String TOPIC = "/topic/meetings/";

	private final TransactionalPublisher publisher;
	private final EventStorageGateway eventStorage;

	public EventService(final TransactionalPublisher publisher,
			final EventStorageGateway eventStorage) {
		this.publisher = publisher;
		this.eventStorage = eventStorage;
	}

	public void publish(final EventInfo event) {
		Objects.requireNonNull(event);

		final EventInfo created = eventStorage.create(event);

		final String destination = TOPIC + created.getMeetingId();
		publisher.send(destination, new EventDto(created));
	}
}
