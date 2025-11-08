package com.cvesters.notula.session;

import java.util.Objects;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.stereotype.Service;

import com.cvesters.notula.session.bdo.SessionInfo;
import com.cvesters.notula.session.bdo.SessionTokens;
import com.cvesters.notula.user.UserService;
import com.cvesters.notula.user.bdo.UserInfo;
import com.cvesters.notula.user.bdo.UserLogin;

@Service
public class SessionService {

	private final UserService userService;
	private final AccessTokenService accessTokenService;

	private final SessionStorageGateway sessionStorageGateway;

	public SessionService(final UserService userService,
			final AccessTokenService jwtService,
			final SessionStorageGateway sessionStorageGateway) {
		this.userService = userService;
		this.accessTokenService = jwtService;
		this.sessionStorageGateway = sessionStorageGateway;
	}

	// TODO: test!!!!
	public SessionTokens create(final UserLogin request) {
		Objects.requireNonNull(request);

		final UserInfo user = userService.findByLogin(request)
				.orElseThrow(() -> new EntityNotFoundException());

		final var newSession = new SessionInfo(user.getId());
		final var createdSession = sessionStorageGateway.create(newSession);
		final String accessToken = accessTokenService.create(createdSession);

		return new SessionTokens(createdSession, accessToken);
	}
}
