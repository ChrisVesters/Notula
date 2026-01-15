package com.cvesters.notula.organisation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.organisation.bdo.OrganisationUserInfo;
import com.cvesters.notula.user.TestUser;

class OrganisationUserServiceTest {

	private static final TestOrganisationUser ORGANISATION_USER = TestOrganisationUser.SPORER_EDUARDO_CHRISTIANSEN;
	private static final TestUser USER = ORGANISATION_USER.getUser();
	private static final Principal PRINCIPAL = USER.principal();

	private final OrganisationUserStorageGateway organisationUserStorage = mock();

	private final OrganisationUserService organisationUserService = new OrganisationUserService(
			organisationUserStorage);

	@Nested
	class GetAll {

		@Test
		void success() {
			final var organisationUsers = List.of(ORGANISATION_USER.info());
			when(organisationUserStorage.findAllByUserId(USER.getId()))
					.thenReturn(organisationUsers);

			final List<OrganisationUserInfo> result = organisationUserService
					.getAll(PRINCIPAL);

			assertThat(result).isEqualTo(organisationUsers);
		}

		@Test
		void organisationUserNotFound() {
			when(organisationUserStorage.findAllByUserId(USER.getId()))
					.thenReturn(Collections.emptyList());

			final List<OrganisationUserInfo> result = organisationUserService
					.getAll(PRINCIPAL);

			assertThat(result).isEmpty();
		}

		@Test
		void principalNull() {
			assertThatThrownBy(() -> organisationUserService.getAll(null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
