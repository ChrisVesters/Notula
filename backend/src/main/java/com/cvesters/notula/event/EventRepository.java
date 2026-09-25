package com.cvesters.notula.event;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import com.cvesters.notula.event.dao.EventDao;

public interface EventRepository extends Repository<EventDao, Long> {

	EventDao save(EventDao event);

	@Query("""
		SELECT e
		FROM events e
		WHERE e.meetingId = :meetingId AND e.revision > :revision
		ORDER BY e.revision
		""")
	List<EventDao> findAllSince(long meetingId, long revision);

	Optional<EventDao> findByMeetingIdAndChangeId(long meetingId, UUID changeId);
}
