package com.cvesters.notula.session.bdo;

import java.time.OffsetDateTime;
import java.util.Objects;

import lombok.Getter;

@Getter
public class SessionInfo {

	private final long id;
	private final long userId;
	// TODO orgId
	// private final Long organisationId;
	private OffsetDateTime activeUntil;

	public SessionInfo(final long id, final long userId, final OffsetDateTime activeUntil) {
		Objects.requireNonNull(activeUntil);

		this.id = id;
		this.userId = userId;
		this.activeUntil = activeUntil;
	}
}
