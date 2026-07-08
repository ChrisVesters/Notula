package com.cvesters.notula.organisation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.common.domain.Email;
import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.common.exception.DuplicateEntityException;
import com.cvesters.notula.common.exception.MissingEntityException;
import com.cvesters.notula.organisation.bdo.OrganisationUserAction;
import com.cvesters.notula.organisation.bdo.OrganisationUserInfo;
import com.cvesters.notula.organisation.bdo.OrganisationUserView;
import com.cvesters.notula.session.TestSession;
import com.cvesters.notula.user.TestUser;
import com.cvesters.notula.user.UserService;
import com.cvesters.notula.user.bdo.UserInfo;

class OrganisationUserServiceTest {

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
	private static final TestOrganisationUser ORGANISATION_USER = TestOrganisationUser.SPORER_EDUARDO_CHRISTIANSEN;
	private static final TestUser USER = ORGANISATION_USER.getUser();
	private static final Principal PRINCIPAL = SESSION.principal();

	private final UserService userService = mock();
	private final OrganisationUserStorageGateway organisationUserStorage = mock();

	private final OrganisationUserService organisationUserService = new OrganisationUserService(
			userService, organisationUserStorage);

	@Nested
	class GetAllForUser {

		@Test
		void success() {
			final var organisationUsers = List.of(ORGANISATION_USER.info());
			when(organisationUserStorage.findAllByUserId(USER.getId()))
					.thenReturn(organisationUsers);

			final List<OrganisationUserInfo> result = organisationUserService
					.getAllForUser(PRINCIPAL);

			assertThat(result).isEqualTo(organisationUsers);
		}

		@Test
		void organisationUserNotFound() {
			when(organisationUserStorage.findAllByUserId(USER.getId()))
					.thenReturn(Collections.emptyList());

			final List<OrganisationUserInfo> result = organisationUserService
					.getAllForUser(PRINCIPAL);

			assertThat(result).isEmpty();
		}

		@Test
		void principalNull() {
			assertThatThrownBy(
					() -> organisationUserService.getAllForUser(null))
							.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class GetAllForOrganisation {

		@Test
		void success() {
			final var organisation = ORGANISATION_USER.getOrganisation();
			final var organisationUsers = List.of(ORGANISATION_USER.view());
			when(organisationUserStorage
					.findAllByOrganisationId(organisation.getId()))
							.thenReturn(organisationUsers);

			final List<OrganisationUserView> result = organisationUserService
					.getAllForOrganisation(PRINCIPAL);

			assertThat(result).isEqualTo(organisationUsers);
		}

		@Test
		void principalNull() {
			assertThatThrownBy(
					() -> organisationUserService.getAllForUser(null))
							.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class Create {

		@Test
		void success() {
			final UserInfo userInfo = USER.info();
			when(userService.findByEmail(USER.getEmail()))
					.thenReturn(Optional.of(userInfo));

			final long userId = USER.getId();
			final long orgId = SESSION.getOrganisation().getId();
			when(organisationUserStorage.findByUserIdAndOrganisationId(userId,
					orgId)).thenReturn(Optional.empty());

			final OrganisationUserInfo created = ORGANISATION_USER.info();
			when(organisationUserStorage.create(argThat(orgUser -> {
				assertThat(orgUser.getId()).isNull();
				assertThat(orgUser.getOrganisationId()).isEqualTo(orgId);
				assertThat(orgUser.getUserId()).isEqualTo(userId);
				return true;
			}))).thenReturn(created);

			final var create = new OrganisationUserAction.Create(
					USER.getEmail());
			final OrganisationUserInfo result = organisationUserService
					.create(SESSION.principal(), create);

			assertThat(result).isEqualTo(created);
		}

		@Test
		void userNotFound() {
			final Email email = USER.getEmail();
			when(userService.findByEmail(email)).thenReturn(Optional.empty());

			final Principal principal = SESSION.principal();
			final var create = new OrganisationUserAction.Create(email);
			assertThatThrownBy(
					() -> organisationUserService.create(principal, create))
							.isInstanceOf(MissingEntityException.class);
		}

		@Test
		void alreadyExists() {
			final UserInfo userInfo = USER.info();
			when(userService.findByEmail(USER.getEmail()))
					.thenReturn(Optional.of(userInfo));

			final long userId = USER.getId();
			final long orgId = SESSION.getOrganisation().getId();
			final OrganisationUserInfo existing = ORGANISATION_USER.info();
			when(organisationUserStorage.findByUserIdAndOrganisationId(userId,
					orgId)).thenReturn(Optional.of(existing));

			final var principal = SESSION.principal();
			final var create = new OrganisationUserAction.Create(
					USER.getEmail());
			assertThatThrownBy(
					() -> organisationUserService.create(principal, create))
							.isInstanceOf(DuplicateEntityException.class);

			verify(organisationUserStorage, never()).create(any());
		}

		@Test
		void actionNull() {
			final Principal principal = SESSION.principal();
			final OrganisationUserAction.Create meeting = null;

			assertThatThrownBy(
					() -> organisationUserService.create(principal, meeting))
							.isInstanceOf(NullPointerException.class);
		}

		@Test
		void principalNull() {
			final Principal principal = null;
			final var create = new OrganisationUserAction.Create(
					USER.getEmail());

			assertThatThrownBy(
					() -> organisationUserService.create(principal, create))
							.isInstanceOf(NullPointerException.class);
		}
	}
}
