package com.cvesters.notula.session;

import java.util.Objects;

import org.springframework.stereotype.Service;

import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.common.exception.MissingEntityException;
import com.cvesters.notula.organisation.OrganisationUserService;
import com.cvesters.notula.session.bdo.SessionCreate;
import com.cvesters.notula.session.bdo.SessionInfo;
import com.cvesters.notula.session.bdo.SessionTokens;
import com.cvesters.notula.session.bdo.SessionUpdate;
import com.cvesters.notula.user.UserService;
import com.cvesters.notula.user.bdo.UserInfo;
import com.cvesters.notula.user.bdo.UserLogin;

@Service
public class SessionService {

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

		final var action = new SessionCreate(user);
		final SessionInfo createdSession = sessionStorage.create(action);
		final String accessToken = accessTokenService.create(createdSession);
		final String refreshToken = action.getRefreshToken();

		return new SessionTokens(createdSession, accessToken, refreshToken);
	}

	public SessionTokens update(final Principal principal,
			final SessionUpdate update) {
		Objects.requireNonNull(principal);
		Objects.requireNonNull(update);

		organisationUserService.getAll(principal)
				.stream()
				.filter(ou -> ou.getOrganisationId() == update.organisationId())
				.findFirst()
				.orElseThrow(MissingEntityException::new);

		final SessionInfo bdo = sessionStorage.findById(update.sessionId())
				.filter(session -> session.getUserId() == principal.userId())
				.orElseThrow(MissingEntityException::new);

		bdo.update(update);

		final SessionInfo session = sessionStorage.update(bdo);
		final String accessToken = accessTokenService.create(session);

		return new SessionTokens(session, accessToken);
	}
}
