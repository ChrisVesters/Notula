package com.cvesters.notula.event;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

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

	public Optional<EventInfo> findByChangeId(final long meetingId,
			final UUID changeId) {
		Objects.requireNonNull(changeId);

		return eventRepository.findByMeetingIdAndChangeId(meetingId, changeId)
				.map(EventDao::toBdo);
	}

	public List<EventInfo> findAllSince(final long meetingId,
			final long revision) {
		return eventRepository.findAllSince(meetingId, revision)
				.stream()
				.map(EventDao::toBdo)
				.toList();
	}
}
