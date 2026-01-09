package com.cvesters.notula.session;

import java.util.Objects;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.cvesters.notula.session.bdo.SessionCreateAction;
import com.cvesters.notula.session.bdo.SessionInfo;
import com.cvesters.notula.session.dao.SessionDao;

@Service
public class SessionStorageGateway {

	private final SessionRepository sessionRepository;

	private final PasswordEncoder passwordEncoder;

	public SessionStorageGateway(final SessionRepository sessionRepository,
			final PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
		this.sessionRepository = sessionRepository;
	}

	public SessionInfo create(final SessionCreateAction sessionInfo) {
		Objects.requireNonNull(sessionInfo);

		final String tokenHash = passwordEncoder
				.encode(sessionInfo.getRefreshToken());
		final var dao = new SessionDao(sessionInfo, tokenHash);
		final SessionDao created = sessionRepository.save(dao);
		return created.toBdo();
	}

}
