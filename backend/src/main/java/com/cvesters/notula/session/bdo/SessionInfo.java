package com.cvesters.notula.session.bdo;

import java.time.OffsetDateTime;
import java.util.Objects;

import lombok.Getter;

@Getter
public class SessionInfo {

	private Long id;
	private long userId;
	private String refreshToken;
	private OffsetDateTime activeUntil;

	// TODO: constructor without id!!!

	public SessionInfo(final Long id, final long userId,
			final String refreshToken, final OffsetDateTime activeUntil) {
		Objects.requireNonNull(refreshToken);
		Objects.requireNonNull(activeUntil);

		this.id = id;
		this.userId = userId;
		this.refreshToken = refreshToken;
		this.activeUntil = activeUntil;
	}
}
