package com.cvesters.notula.event;

import org.springframework.data.repository.Repository;

import com.cvesters.notula.event.dao.EventDao;

public interface EventRepository extends Repository<EventDao, Long> {

	EventDao save(EventDao event);
}
