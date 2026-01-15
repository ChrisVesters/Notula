package com.cvesters.notula.session;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import com.cvesters.notula.session.dao.SessionDao;

public interface SessionRepository extends Repository<SessionDao, Long> {

	Optional<SessionDao> findById(final long id);

	SessionDao save(final SessionDao sessionDao);
}
