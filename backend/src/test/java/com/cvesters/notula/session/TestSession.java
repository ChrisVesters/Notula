package com.cvesters.notula.session;

import java.time.OffsetDateTime;
import java.util.Optional;

import lombok.Getter;

import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.organisation.TestOrganisation;
import com.cvesters.notula.session.bdo.SessionInfo;
import com.cvesters.notula.user.TestUser;

// TODO: find a way to linke to TestOrganisationUser if possible!
@Getter
public enum TestSession {
	EDUARDO_CHRISTIANSEN_SPORER(1L, TestUser.EDUARDO_CHRISTIANSEN,
			TestOrganisation.SPORER, "abc7775",
			OffsetDateTime.now().plusDays(7)),
	KRISTINA_THIEL(2L, TestUser.KRISTINA_THIEL, null, "eff74def",
			OffsetDateTime.now().plusDays(30)),
	ALISON_DACH(3L, TestUser.ALISON_DACH, null, "ghj89tyu",
			OffsetDateTime.now().plusDays(15)),
	EDUARDO_CHRISTIANSEN(7L, TestUser.EDUARDO_CHRISTIANSEN, null, "ddef741",
			OffsetDateTime.now().minusDays(1)),
	ALISON_DACH_GLOVER(8L, TestUser.ALISON_DACH, TestOrganisation.GLOVER, "ad98gh3",
			OffsetDateTime.now().plusDays(3));

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

	public Principal principal() {
		final long userId = user.getId();
		final Long organisationId = Optional.ofNullable(organisation)
				.map(TestOrganisation::getId)
				.orElse(null);

		return new Principal(userId, organisationId);
	}
}
