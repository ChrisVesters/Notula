package com.cvesters.notula.session.bdo;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.Validate;

import lombok.Getter;

@Getter
public class SessionInfo {

	private final long id;
	private final long userId;
	private Long organisationId;
	private OffsetDateTime activeUntil;

	public SessionInfo(final long id, final long userId,
			final OffsetDateTime activeUntil) {
		this(id, userId, null, activeUntil);
	}

	public SessionInfo(final long id, final long userId,
			final Long organisationId, final OffsetDateTime activeUntil) {
		Objects.requireNonNull(activeUntil);

		this.id = id;
		this.userId = userId;
		this.organisationId = organisationId;
		this.activeUntil = activeUntil;
	}

	public void update(final SessionUpdate update) {
		Objects.requireNonNull(update);
		Validate.isTrue(id == update.sessionId());

		this.organisationId = update.organisationId();
	}

	public Optional<Long> getOrganisationId() {
		return Optional.ofNullable(organisationId);
	}
}
