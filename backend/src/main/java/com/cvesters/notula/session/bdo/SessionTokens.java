package com.cvesters.notula.session.bdo;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Optional;

import lombok.Getter;

@Getter
public class SessionTokens {

	private final long id;
	private final String accessToken;
	private final String refreshToken;
	private final OffsetDateTime activeUntil;

	public SessionTokens(final SessionInfo session, final String accessToken) {
		this(session, accessToken, null);
	}

	public SessionTokens(final SessionInfo session, final String accessToken,
			final String refreshToken) {
		Objects.requireNonNull(session);
		Objects.requireNonNull(accessToken);

		this.id = session.getId();
		this.activeUntil = session.getActiveUntil();
		this.accessToken = accessToken;
		this.refreshToken = refreshToken;
	}

	public Optional<String> getRefreshToken() {
		return Optional.ofNullable(refreshToken);
	}
}
