package com.cvesters.notula.organisation;

import lombok.Getter;

import com.cvesters.notula.organisation.bdo.OrganisationUserInfo;
import com.cvesters.notula.user.TestUser;

@Getter
public enum TestOrganisationUser {
	SPORER_EDUARDO_CHRISTIANSEN(1L, TestOrganisation.SPORER,
			TestUser.EDUARDO_CHRISTIANSEN),
	GLOVER_ALISON_DACH(3L, TestOrganisation.GLOVER, TestUser.ALISON_DACH),
	HEUL_ALISON_DACH(4L, TestOrganisation.HEUL, TestUser.ALISON_DACH);

	private final long id;
	private final TestOrganisation organisation;
	private final TestUser user;

	TestOrganisationUser(final long id, final TestOrganisation organisation,
			final TestUser user) {
		this.id = id;
		this.organisation = organisation;
		this.user = user;
	}

	public OrganisationUserInfo info() {
		return new OrganisationUserInfo(id, organisation.getId(), user.getId());
	}
}
