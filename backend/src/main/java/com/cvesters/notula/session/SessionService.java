package com.cvesters.notula.session;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.common.exception.MissingEntityException;
import com.cvesters.notula.organisation.OrganisationUserService;
import com.cvesters.notula.session.bdo.SessionInfo;
import com.cvesters.notula.session.bdo.SessionTokens;
import com.cvesters.notula.session.bdo.SessionUpdate;
import com.cvesters.notula.user.UserService;
import com.cvesters.notula.user.bdo.UserInfo;
import com.cvesters.notula.user.bdo.UserLogin;

@Service
public class SessionService {

	private static final SecureRandom RANDOM = new SecureRandom();

	private final UserService userService;
	private final OrganisationUserService organisationUserService;
	private final AccessTokenService accessTokenService;

	private final SessionStorageGateway sessionStorage;

	public SessionService(final UserService userService,
			final OrganisationUserService organisationUserService,
			final AccessTokenService accessTokenService,
			final SessionStorageGateway sessionStorageGateway) {
		this.userService = userService;
		this.organisationUserService = organisationUserService;
		this.accessTokenService = accessTokenService;
		this.sessionStorage = sessionStorageGateway;
	}

	public SessionTokens create(final UserLogin request) {
		Objects.requireNonNull(request);

		final UserInfo user = userService.findByLogin(request)
				.orElseThrow(MissingEntityException::new);

		final var action = new SessionInfo(user.getId(), null);
		final String refreshToken = generateRefreshToken();

		final SessionInfo createdSession = sessionStorage.create(action,
				refreshToken);
		final String accessToken = accessTokenService.create(createdSession);

		return new SessionTokens(createdSession, accessToken, refreshToken);
	}

	public SessionTokens update(final Principal principal, final long sessionId,
			final SessionUpdate update) {
		Objects.requireNonNull(principal);
		Objects.requireNonNull(update);

		organisationUserService.getAll(principal)
				.stream()
				.filter(ou -> ou.getOrganisationId() == update.organisationId())
				.findFirst()
				.orElseThrow(MissingEntityException::new);

		final SessionInfo bdo = sessionStorage.findById(sessionId)
				.filter(session -> session.getUserId() == principal.userId())
				.orElseThrow(MissingEntityException::new);

		bdo.update(update);

		final SessionInfo session = sessionStorage.update(bdo);
		final String accessToken = accessTokenService.create(session);

		return new SessionTokens(session, accessToken);
	}

	public SessionTokens refresh(final long sessionId,
			final String refreshToken) {
		Objects.requireNonNull(refreshToken);

		final SessionInfo bdo = sessionStorage
				.findByIdAndRefreshToken(sessionId, refreshToken)
				.orElseThrow(MissingEntityException::new);

		bdo.refresh();
		final String newToken = generateRefreshToken();

		final SessionInfo session = sessionStorage.update(bdo, newToken);
		final String accessToken = accessTokenService.create(session);

		return new SessionTokens(session, accessToken, newToken);
	}

	private static String generateRefreshToken() {
		final var bytes = new byte[64];
		RANDOM.nextBytes(bytes);

		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}
}
