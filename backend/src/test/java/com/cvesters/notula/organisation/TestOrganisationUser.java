package com.cvesters.notula.organisation;

import java.util.Arrays;
import java.util.List;

import lombok.Getter;

import com.cvesters.notula.organisation.bdo.OrganisationUserInfo;
import com.cvesters.notula.organisation.bdo.OrganisationUserRole;
import com.cvesters.notula.organisation.bdo.OrganisationUserView;
import com.cvesters.notula.user.TestUser;

@Getter
public enum TestOrganisationUser {
	SPORER_EDUARDO_CHRISTIANSEN(1L, TestOrganisation.SPORER,
			TestUser.EDUARDO_CHRISTIANSEN, OrganisationUserRole.ADMIN),
	SPORER_KRISTINA_THIEL(2L, TestOrganisation.SPORER, TestUser.KRISTINA_THIEL,
			OrganisationUserRole.MEMBER),
	GLOVER_ALISON_DACH(3L, TestOrganisation.GLOVER, TestUser.ALISON_DACH,
			OrganisationUserRole.ADMIN),
	HEUL_ALISON_DACH(4L, TestOrganisation.HEUL, TestUser.ALISON_DACH,
			OrganisationUserRole.MEMBER);

	private final long id;
	private final TestOrganisation organisation;
	private final TestUser user;
	private final OrganisationUserRole role;

	TestOrganisationUser(final long id, final TestOrganisation organisation,
			final TestUser user, final OrganisationUserRole role) {
		this.id = id;
		this.organisation = organisation;
		this.user = user;
		this.role = role;
	}

	public static List<TestOrganisationUser> ofOrganisation(
			final TestOrganisation organisation) {
		return Arrays.stream(TestOrganisationUser.values())
				.filter(user -> user.organisation.equals(organisation))
				.toList();
	}

	public static TestOrganisationUser ofUserAndOrganisation(
			final TestUser user, final TestOrganisation organisation) {

		return Arrays.stream(TestOrganisationUser.values())
				.filter(u -> u.user.equals(user))
				.filter(u -> u.organisation.equals(organisation))
				.findFirst()
				.orElse(null);
	}

	public OrganisationUserInfo info() {
		return new OrganisationUserInfo(id, organisation.getId(), user.getId(),
				role);
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

			@Override
			public OrganisationUserRole getRole() {
				return role;
			}

		};
	}
}
