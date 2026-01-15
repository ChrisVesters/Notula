package com.cvesters.notula.session;

import java.util.Objects;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.cvesters.notula.common.exception.MissingEntityException;
import com.cvesters.notula.session.bdo.SessionCreate;
import com.cvesters.notula.session.bdo.SessionInfo;
import com.cvesters.notula.session.bdo.SessionUpdate;
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

	public Optional<SessionInfo> findById(final long sessionId) {
		return sessionRepository.findById(sessionId).map(SessionDao::toBdo);
	}

	public SessionInfo create(final SessionCreate sessionInfo) {
		Objects.requireNonNull(sessionInfo);

		final String tokenHash = passwordEncoder
				.encode(sessionInfo.getRefreshToken());
		final var dao = new SessionDao(sessionInfo, tokenHash);
		final SessionDao created = sessionRepository.save(dao);
		return created.toBdo();
	}

	public SessionInfo update(final SessionUpdate update) {
		Objects.requireNonNull(update);

		final SessionDao dao = sessionRepository.findById(update.sessionId())
				.orElseThrow(MissingEntityException::new);

		dao.setOrganisationId(update.organisationId());
		final SessionDao updated = sessionRepository.save(dao);

		return updated.toBdo();
	}
}
