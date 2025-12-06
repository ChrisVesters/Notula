package com.cvesters.notula.session;

import java.util.Objects;

import org.springframework.stereotype.Service;

import com.cvesters.notula.session.bdo.SessionInfo;
import com.cvesters.notula.session.dao.SessionDao;

@Service
public class SessionStorageGateway {

	private final SessionRepository sessionRepository;

	public SessionStorageGateway(SessionRepository sessionRepository) {
		this.sessionRepository = sessionRepository;
	}
	
	public SessionInfo create(final SessionInfo sessionInfo) {
		Objects.requireNonNull(sessionInfo);
		
		// TODO: refresh token needs to be encoded/hashed.
		final var dao = new SessionDao(sessionInfo);
		final SessionDao created = sessionRepository.save(dao);
		return created.toBdo();
	}

}
