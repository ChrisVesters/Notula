package com.cvesters.notula.session;

import java.time.OffsetDateTime;
import java.time.Period;
import java.util.Optional;

import lombok.Getter;

import com.cvesters.notula.organisation.TestOrganisation;
import com.cvesters.notula.session.bdo.SessionInfo;
import com.cvesters.notula.user.TestUser;

@Getter
public enum TestSession {
	EDUARDO_CHRISTIANSEN_DEKSTOP(1L, TestUser.EDUARDO_CHRISTIANSEN,
			TestOrganisation.SPORER, "abc7775",
			OffsetDateTime.now().plus(Period.ofDays(7))),
	EDUARDO_CHRISTIANSEN_MOBILE(7L, TestUser.EDUARDO_CHRISTIANSEN, null,
			"ddef741", OffsetDateTime.now().minus(Period.ofDays(1)));

	private final long id;
	private final TestUser user;
	private final TestOrganisation organisation;
	private final String refreshToken;
	private final OffsetDateTime activeUntil;

	TestSession(final long id, final TestUser user,
			final TestOrganisation organisation, final String refreshToken,
			final OffsetDateTime activeUntil) {
		this.id = id;
		this.user = user;
		this.organisation = organisation;
		this.refreshToken = refreshToken;
		this.activeUntil = activeUntil;
	}

	public SessionInfo info() {
		final long userId = user.getId();
		final Long organisationId = Optional.ofNullable(organisation)
				.map(TestOrganisation::getId)
				.orElse(null);

		return new SessionInfo(id, userId, organisationId, activeUntil);
	}
}
