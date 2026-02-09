package com.cvesters.notula.session;

import java.util.Objects;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.cvesters.notula.common.exception.MissingEntityException;
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

	public Optional<SessionInfo> findById(final long sessionId) {
		return sessionRepository.findById(sessionId).map(SessionDao::toBdo);
	}

	public Optional<SessionInfo> findByIdAndRefreshToken(final long sessionId,
			final String refreshToken) {
		Objects.requireNonNull(refreshToken);

		return sessionRepository.findById(sessionId)
				.filter(session -> passwordEncoder.matches(refreshToken,
						session.getRefreshToken()))
				.map(SessionDao::toBdo);
	}

	public SessionInfo create(final SessionInfo sessionInfo,
			final String refreshToken) {
		Objects.requireNonNull(sessionInfo);
		Objects.requireNonNull(refreshToken);

		final String tokenHash = passwordEncoder.encode(refreshToken);
		final var dao = new SessionDao(sessionInfo, tokenHash);
		final SessionDao created = sessionRepository.save(dao);
		return created.toBdo();
	}

	public SessionInfo update(final SessionInfo update) {
		Objects.requireNonNull(update);

		final SessionDao dao = sessionRepository.findById(update.getId())
				.orElseThrow(MissingEntityException::new);

		dao.update(update);
		final SessionDao updated = sessionRepository.save(dao);

		return updated.toBdo();
	}

	public SessionInfo update(final SessionInfo update,
			final String refreshToken) {
		Objects.requireNonNull(update);
		Objects.requireNonNull(refreshToken);

		final SessionDao dao = sessionRepository.findById(update.getId())
				.orElseThrow(MissingEntityException::new);

		final String tokenHash = passwordEncoder.encode(refreshToken);

		dao.update(update, tokenHash);
		final SessionDao updated = sessionRepository.save(dao);

		return updated.toBdo();
	}
}
