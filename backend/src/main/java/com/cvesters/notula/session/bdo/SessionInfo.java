package com.cvesters.notula.session.bdo;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.Validate;

import lombok.Getter;

@Getter
public class SessionInfo {

	private static final Duration ACTIVE_DURATION = Duration.ofDays(7);

	private final Long id;
	private final long userId;
	private Long organisationId;
	private OffsetDateTime activeUntil;

	public SessionInfo(final long userId, final Long organisationId) {
		this(null, userId, organisationId,
				OffsetDateTime.now().plus(ACTIVE_DURATION));
	}

	public SessionInfo(final Long id, final long userId,
			final Long organisationId, final OffsetDateTime activeUntil) {
		Objects.requireNonNull(activeUntil);

		this.id = id;
		this.userId = userId;
		this.organisationId = organisationId;
		this.activeUntil = activeUntil;
	}

	public void update(final SessionUpdate update) {
		Objects.requireNonNull(update);
		Validate.validState(isActive());

		this.organisationId = update.organisationId();
	}

	public void refresh() {
		Validate.validState(isActive());

		this.activeUntil = OffsetDateTime.now().plus(ACTIVE_DURATION);
	}

	public long getId() {
		Validate.validState(id != null);

		return id;
	}

	public Optional<Long> getOrganisationId() {
		return Optional.ofNullable(organisationId);
	}

	public void invactivate() {
		Validate.validState(isActive());

		this.activeUntil = OffsetDateTime.now();
	}

	public boolean isActive() {
		return this.activeUntil.isAfter(OffsetDateTime.now());
	}
}
