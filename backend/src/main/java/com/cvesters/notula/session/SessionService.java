package com.cvesters.notula.session;

import java.util.Objects;

import org.springframework.stereotype.Service;

import com.cvesters.notula.common.exception.MissingEntityException;
import com.cvesters.notula.session.bdo.SessionCreateAction;
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

	public SessionTokens create(final UserLogin request) {
		Objects.requireNonNull(request);

		final UserInfo user = userService.findByLogin(request)
				.orElseThrow(MissingEntityException::new);

		final var action = new SessionCreateAction(user);
		final String refreshToken = action.getRefreshToken();
		final var createdSession = sessionStorageGateway.create(action);
		final String accessToken = accessTokenService.create(createdSession);

		return new SessionTokens(createdSession, accessToken, refreshToken);
	}
}
