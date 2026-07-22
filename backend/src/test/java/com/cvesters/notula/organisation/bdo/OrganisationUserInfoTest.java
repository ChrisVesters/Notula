package com.cvesters.notula.organisation.bdo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.organisation.TestOrganisation;
import com.cvesters.notula.organisation.TestOrganisationUser;
import com.cvesters.notula.user.TestUser;

class OrganisationUserInfoTest {

	private static final TestOrganisationUser ORGANISATION_USER = TestOrganisationUser.SPORER_EDUARDO_CHRISTIANSEN;
	private static final TestOrganisation ORGANISATION = ORGANISATION_USER
			.getOrganisation();
	private static final TestUser USER = ORGANISATION_USER.getUser();

	@Nested
	class Constructor {

		@Test
		void withoutId() {
			final var orgId = ORGANISATION.getId();
			final var userId = USER.getId();
			final var role = ORGANISATION_USER.getRole();

			final var organisationUserInfo = new OrganisationUserInfo(orgId,
					userId, role);

			assertThat(organisationUserInfo.getId()).isNull();
			assertThat(organisationUserInfo.getOrganisationId())
					.isEqualTo(ORGANISATION.getId());
			assertThat(organisationUserInfo.getUserId())
					.isEqualTo(USER.getId());
			assertThat(organisationUserInfo.getRole())
					.isEqualTo(ORGANISATION_USER.getRole());
		}

		@Test
		void withId() {
			final var orgUserId = ORGANISATION_USER.getId();
			final var orgId = ORGANISATION.getId();
			final var userId = USER.getId();
			final var role = ORGANISATION_USER.getRole();

			final var organisationUserInfo = new OrganisationUserInfo(orgUserId,
					orgId, userId, role);

			assertThat(organisationUserInfo.getId())
					.isEqualTo(ORGANISATION_USER.getId());
			assertThat(organisationUserInfo.getOrganisationId())
					.isEqualTo(ORGANISATION.getId());
			assertThat(organisationUserInfo.getUserId())
					.isEqualTo(USER.getId());
			assertThat(organisationUserInfo.getRole())
					.isEqualTo(ORGANISATION_USER.getRole());
		}

		@Test
		void idNull() {
			final Long orgUserId = null;
			final var orgId = ORGANISATION.getId();
			final var userId = USER.getId();
			final var role = ORGANISATION_USER.getRole();

			final var organisationUserInfo = new OrganisationUserInfo(orgUserId,
					orgId, userId, role);

			assertThat(organisationUserInfo.getId()).isNull();
			assertThat(organisationUserInfo.getOrganisationId())
					.isEqualTo(ORGANISATION.getId());
			assertThat(organisationUserInfo.getUserId())
					.isEqualTo(USER.getId());
			assertThat(organisationUserInfo.getRole())
					.isEqualTo(ORGANISATION_USER.getRole());
		}
	}
}
