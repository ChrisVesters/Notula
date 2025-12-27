package com.cvesters.notula.organisation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.organisation.bdo.OrganisationInfo;
import com.cvesters.notula.user.TestUser;

class OrganisationServiceTest {

	private static final TestOrganisationUser ORGANISATION_USER = TestOrganisationUser.SPORER_EDUARDO_CHRISTIANSEN;
	private static final TestOrganisation ORGANISATION = ORGANISATION_USER
			.getOrganisation();
	private static final TestUser USER = ORGANISATION_USER.getUser();

	private final OrganisationStorageGateway organisationStorage = mock();
	private final OrganisationUserStorageGateway organisationUserStorage = mock();

	private final OrganisationService organisationService = new OrganisationService(
			organisationStorage, organisationUserStorage);

	@Nested
	class GetAll {

		@Test
		void success() {
			final Principal principal = USER.principal();

			when(organisationUserStorage.findAllByUserId(USER.getId()))
					.thenReturn(List.of(ORGANISATION_USER.info()));

			final List<OrganisationInfo> organisations = List
					.of(ORGANISATION.info());
			when(organisationStorage.findAllById(List.of(ORGANISATION.getId())))
					.thenReturn(organisations);

			final List<OrganisationInfo> result = organisationService
					.getAll(principal);

			assertThat(result).isEqualTo(organisations);
		}

		@Test
		void organisationUserNotFound() {
			final Principal principal = USER.principal();

			when(organisationUserStorage.findAllByUserId(USER.getId()))
					.thenReturn(Collections.emptyList());

			final List<OrganisationInfo> result = organisationService
					.getAll(principal);

			assertThat(result).isEmpty();
		}

		@Test
		void organisationNotFound() {
			final Principal principal = USER.principal();

			when(organisationUserStorage.findAllByUserId(USER.getId()))
					.thenReturn(List.of(ORGANISATION_USER.info()));

			when(organisationStorage.findAllById(List.of(ORGANISATION.getId())))
					.thenReturn(Collections.emptyList());

			final List<OrganisationInfo> result = organisationService
					.getAll(principal);

			assertThat(result).isEmpty();
		}

		@Test
		void principalNull() {
			final Principal principal = null;

			assertThatThrownBy(() -> organisationService.getAll(principal))
					.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class Create {

		@Test
		void success() {
			final Principal principal = USER.principal();
			final OrganisationInfo organisation = new OrganisationInfo(
					ORGANISATION.getName());

			final OrganisationInfo created = ORGANISATION.info();
			when(organisationStorage.create(organisation)).thenReturn(created);

			final OrganisationInfo result = organisationService
					.create(principal, organisation);

			assertThat(created).isEqualTo(result);

			verify(organisationUserStorage).create(argThat((orgUserInfo -> {
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
