package com.cvesters.notula.event;

import java.util.Objects;

import org.springframework.stereotype.Service;

import com.cvesters.notula.event.bdo.EventInfo;
import com.cvesters.notula.event.dao.EventDao;

@Service
public class EventStorageGateway {

	private final EventRepository eventRepository;

	public EventStorageGateway(final EventRepository eventRepository) {
		this.eventRepository = eventRepository;
	}

	public EventInfo create(final EventInfo event) {
		Objects.requireNonNull(event);

		final var dao = new EventDao(event);
		final EventDao saved = eventRepository.save(dao);
		return saved.toBdo();
	}
}
