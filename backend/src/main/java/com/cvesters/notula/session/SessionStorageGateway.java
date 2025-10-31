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
		
		final var dao = new SessionDao(sessionInfo.getUserId(),
				sessionInfo.getRefreshToken(), sessionInfo.getActiveUntil());
		final SessionDao created = sessionRepository.save(dao);
		return created.toBdo();
	}

}
