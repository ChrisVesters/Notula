package com.cvesters.notula.session;

import org.springframework.data.repository.Repository;

import com.cvesters.notula.session.dao.SessionDao;

public interface SessionRepository extends Repository<SessionDao, Long> {

	SessionDao save(final SessionDao sessionDao);
}
