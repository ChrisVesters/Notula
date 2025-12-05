package com.cvesters.notula.organisation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.organisation.bdo.OrganisationInfo;
import com.cvesters.notula.user.TestUser;

class OrganisationServiceTest {

	private static final TestOrganisation ORGANISATION = TestOrganisation.SPORER;
	private static final TestUser USER = TestUser.EDUARDO_CHRISTIANSEN;

	private final OrganisationUserService organisationUserService = mock();
	private final OrganisationStorageGateway organisationStorageGateway = mock();

	private final OrganisationService organisationService = new OrganisationService(
			organisationUserService, organisationStorageGateway);

	@Nested
	class Create {

		@Test
		void success() {
			final Principal principal = USER.principal();
			final OrganisationInfo organisation = new OrganisationInfo(
					ORGANISATION.getName());

			final OrganisationInfo created = ORGANISATION.info();
			when(organisationStorageGateway.create(organisation))
					.thenReturn(created);

			final OrganisationInfo result = organisationService
					.create(principal, organisation);

			assertThat(created).isEqualTo(result);

			verify(organisationUserService).create(argThat((orgUserInfo -> {
				assertThat(orgUserInfo.getId()).isNull();
				assertThat(orgUserInfo.getOrganisationId())
						.isEqualTo(ORGANISATION.getId());
				assertThat(orgUserInfo.getUserId()).isEqualTo(USER.getId());
				return true;
			})));
		}

		@Test
		void organisationNull() {
			final Principal principal = USER.principal();
			final OrganisationInfo organisation = null;

			assertThatThrownBy(
					() -> organisationService.create(principal, organisation))
							.isInstanceOf(NullPointerException.class);
		}

		@Test
		void principalNull() {
			final Principal principal = null;
			final OrganisationInfo organisation = new OrganisationInfo(
					ORGANISATION.getName());

			assertThatThrownBy(
					() -> organisationService.create(principal, organisation))
							.isInstanceOf(NullPointerException.class);
		}
	}
}
