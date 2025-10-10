package com.cvesters.notula.session;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import com.cvesters.notula.session.bdo.SessionInfo;
import com.cvesters.notula.session.bdo.UserLogin;

@Service
public class SessionService {

	private final AuthenticationManager authManager;

	private final SessionStorageGateway sessionStorageGateway;

	public SessionService(final AuthenticationManager authenticationManager,
			final SessionStorageGateway sessionStorageGateway) {
		this.authManager = authenticationManager;
		this.sessionStorageGateway = sessionStorageGateway;
	}

	public SessionInfo create(final UserLogin request) {
		Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));

        if (!auth.isAuthenticated()) {
            throw new BadCredentialsException("Invalid credentials");
        }

		return null;
		// TODO: implement session creation/authentication flow
	}
}
