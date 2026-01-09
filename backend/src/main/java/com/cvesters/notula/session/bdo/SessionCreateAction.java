package com.cvesters.notula.session.bdo;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Objects;

import lombok.Getter;

import com.cvesters.notula.user.bdo.UserInfo;

@Getter
public class SessionCreateAction {

	private static final SecureRandom RANDOM = new SecureRandom();
	private static final Duration DURATION = Duration.ofDays(7);

	private final long userId;
	private final String refreshToken;
	private final OffsetDateTime activeUntil;

	public SessionCreateAction(final UserInfo user) {
		Objects.requireNonNull(user);

		this.userId = user.getId();
		this.refreshToken = generateToken();
		this.activeUntil = OffsetDateTime.now().plus(DURATION);
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
