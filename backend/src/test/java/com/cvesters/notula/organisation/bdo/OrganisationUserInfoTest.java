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
			final var organisationUserInfo = new OrganisationUserInfo(
					ORGANISATION.getId(), USER.getId());

			assertThat(organisationUserInfo.getId()).isNull();
			assertThat(organisationUserInfo.getOrganisationId())
					.isEqualTo(ORGANISATION.getId());
			assertThat(organisationUserInfo.getUserId())
					.isEqualTo(USER.getId());
		}

		@Test
		void withId() {
			final var organisationUserInfo = new OrganisationUserInfo(
					ORGANISATION_USER.getId(), ORGANISATION.getId(),
					USER.getId());

			assertThat(organisationUserInfo.getId())
					.isEqualTo(ORGANISATION_USER.getId());
			assertThat(organisationUserInfo.getOrganisationId())
					.isEqualTo(ORGANISATION.getId());
			assertThat(organisationUserInfo.getUserId())
					.isEqualTo(USER.getId());
		}

		@Test
		void idNull() {
			final var organisationUserInfo = new OrganisationUserInfo(null,
					ORGANISATION.getId(), USER.getId());

			assertThat(organisationUserInfo.getId()).isNull();
			assertThat(organisationUserInfo.getOrganisationId())
					.isEqualTo(ORGANISATION.getId());
			assertThat(organisationUserInfo.getUserId())
					.isEqualTo(USER.getId());
		}
	}
}
