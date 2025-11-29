package com.cvesters.notula.organisation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.organisation.bdo.OrganisationUserInfo;
import com.cvesters.notula.user.TestUser;

class OrganisationUserServiceTest {

	private static final TestOrganisationUser ORGANISATION_USER = TestOrganisationUser.SPORER_EDUARDO_CHRISTIANSEN;
	private static final TestOrganisation ORGANISATION = ORGANISATION_USER
			.getOrganisation();
	private static final TestUser USER = ORGANISATION_USER.getUser();

	private final OrganisationUserStorageGateway organisationUserStorageGateway = mock();

	private final OrganisationUserService organisationUserService = new OrganisationUserService(
			organisationUserStorageGateway);

	@Nested
	class Create {

		@Test
		void success() {
			final OrganisationUserInfo organisationUser = new OrganisationUserInfo(
					ORGANISATION.getId(), USER.getId());

			final OrganisationUserInfo created = ORGANISATION_USER.info();
			when(organisationUserStorageGateway.create(organisationUser))
					.thenReturn(created);

			final OrganisationUserInfo result = organisationUserService
					.create(organisationUser);

			assertThat(created).isEqualTo(result);
		}

		@Test
		void organisationUserNull() {
			assertThatThrownBy(() -> organisationUserService.create(null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
