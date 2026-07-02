package com.cvesters.notula.organisation;

import java.util.Arrays;
import java.util.List;

import lombok.Getter;

import com.cvesters.notula.organisation.bdo.OrganisationUserInfo;
import com.cvesters.notula.organisation.bdo.OrganisationUserView;
import com.cvesters.notula.user.TestUser;

@Getter
public enum TestOrganisationUser {
	SPORER_EDUARDO_CHRISTIANSEN(1L, TestOrganisation.SPORER,
			TestUser.EDUARDO_CHRISTIANSEN),
	SPORER_KRISTINA_THIEL(2L, TestOrganisation.SPORER, TestUser.KRISTINA_THIEL),
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

	public static List<TestOrganisationUser> ofOrganisation(
			final TestOrganisation organisation) {
		return Arrays.stream(TestOrganisationUser.values())
				.filter(user -> user.organisation.equals(organisation))
				.toList();
	}

	public OrganisationUserInfo info() {
		return new OrganisationUserInfo(id, organisation.getId(), user.getId());
	}

	public OrganisationUserView view() {
		return new OrganisationUserView() {

			@Override
			public long getId() {
				return id;
			}

			@Override
			public long getOrganisationId() {
				return organisation.getId();
			}

			@Override
			public long getUserId() {
				return user.getId();
			}

			@Override
			public String getEmail() {
				return user.getEmail().value();
			}

		} ;
	}
}
