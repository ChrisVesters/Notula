package com.cvesters.notula.session;

import org.springframework.stereotype.Service;

@Service
public class SessionStorageGateway {

	private final SessionRepository sessionRepository;

	public SessionStorageGateway(SessionRepository sessionRepository) {
		this.sessionRepository = sessionRepository;
	}
	
}
