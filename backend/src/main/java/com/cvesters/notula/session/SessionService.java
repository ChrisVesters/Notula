package com.cvesters.notula.session;

import java.util.Objects;

import org.springframework.stereotype.Service;

import com.cvesters.notula.session.bdo.SessionInfo;
import com.cvesters.notula.session.bdo.SessionLogin;
import com.cvesters.notula.user.UserService;

@Service
public class SessionService {

	private final UserService userService;
	private final JwtService jwtService;

	private final SessionStorageGateway sessionStorageGateway;

	public SessionService(final UserService userService,
			final JwtService jwtService,
			final SessionStorageGateway sessionStorageGateway) {
		this.userService = userService;
		this.jwtService = jwtService;
		this.sessionStorageGateway = sessionStorageGateway;
	}

	// SessionTokens
	public SessionInfo create(final SessionLogin request) {
		Objects.requireNonNull(request);

		// Optional<UserInfo> userService.findByLogin(request);

		// TODO: create a session in the database and return the tokens
		// associated with it.
		// Authentication auth = authManager.authenticate(
		// new UsernamePasswordAuthenticationToken(req.getEmail(),
		// req.getPassword()));

		// if (!auth.isAuthenticated()) {
		// throw new BadCredentialsException("Invalid credentials");
		// }

		return null;
		// TODO: implement session creation/authentication flow
	}
}
