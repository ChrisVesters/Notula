package com.cvesters.notula.session.bdo;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Objects;

import lombok.Getter;

@Getter
public class SessionInfo {

	private static final SecureRandom RANDOM = new SecureRandom();
	private static final Duration DURATION = Duration.ofDays(7);

	private final Long id;
	private final long userId;
	private String refreshToken;
	private OffsetDateTime activeUntil;

	public SessionInfo(final long userId) {
		final String token = generateToken();
		final OffsetDateTime activeUntil = OffsetDateTime.now().plus(DURATION);

		this(null, userId, token, activeUntil);
	}

	public SessionInfo(final Long id, final long userId,
			final String refreshToken, final OffsetDateTime activeUntil) {
		Objects.requireNonNull(refreshToken);
		Objects.requireNonNull(activeUntil);

		this.id = id;
		this.userId = userId;
		this.refreshToken = refreshToken;
		this.activeUntil = activeUntil;
	}

	private static String generateToken() {
		final byte[] bytes = new byte[64];
		RANDOM.nextBytes(bytes);

		final String token = Base64.getUrlEncoder()
				.withoutPadding()
				.encodeToString(bytes);
		return token;
	}
}
