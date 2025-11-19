package com.cvesters.notula.session.bdo;

import java.time.OffsetDateTime;
import java.util.Objects;

import lombok.Getter;

@Getter
public class SessionTokens {
	
	private final long id;
	private final String accessToken;
	private final String refreshToken;
	private final OffsetDateTime activeUntil;

	public SessionTokens(final SessionInfo session, final String accessToken) {
		Objects.requireNonNull(session);
		Objects.requireNonNull(session.getId());
		Objects.requireNonNull(accessToken);

		this.id = session.getId();
		this.refreshToken = session.getRefreshToken();
		this.activeUntil = session.getActiveUntil();
		this.accessToken = accessToken;
	}
}
