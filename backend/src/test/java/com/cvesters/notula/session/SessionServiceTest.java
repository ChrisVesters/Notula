package com.cvesters.notula.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.common.exception.MissingEntityException;
import com.cvesters.notula.organisation.OrganisationUserService;
import com.cvesters.notula.organisation.TestOrganisation;
import com.cvesters.notula.organisation.TestOrganisationUser;
import com.cvesters.notula.organisation.bdo.OrganisationUserInfo;
import com.cvesters.notula.session.bdo.SessionInfo;
import com.cvesters.notula.session.bdo.SessionTokens;
import com.cvesters.notula.session.bdo.SessionUpdate;
import com.cvesters.notula.user.TestUser;
import com.cvesters.notula.user.UserService;
import com.cvesters.notula.user.bdo.UserInfo;
import com.cvesters.notula.user.bdo.UserLogin;

class SessionServiceTest {

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
	private static final TestUser USER = SESSION.getUser();
	private static final String ACCESS_TOKEN = "access";

	private final UserService userService = mock();
	private final OrganisationUserService organisationUserService = mock();
	private final AccessTokenService accessTokenService = mock();
	private final SessionStorageGateway sessionStorageGateway = mock();

	private final SessionService sessionService = new SessionService(
			userService, organisationUserService, accessTokenService,
			sessionStorageGateway);

	@Nested
	class Create {

		@Test
		void success() {
			final UserLogin login = USER.login();
			final UserInfo userInfo = USER.info();
			when(userService.findByLogin(login))
					.thenReturn(Optional.of(userInfo));

			final SessionInfo createdSession = SESSION.info();
			when(sessionStorageGateway.create(argThat(create -> {
				assertThat(create.getUserId()).isEqualTo(USER.getId());
				assertThat(create.getRefreshToken()).isNotNull();
				assertThat(create.getActiveUntil()).isNotNull();
				return true;
			}))).thenReturn(createdSession);

			when(accessTokenService.create(createdSession))
					.thenReturn(ACCESS_TOKEN);

			final SessionTokens tokens = sessionService.create(login);

			assertThat(tokens.getId()).isEqualTo(SESSION.getId());
			assertThat(tokens.getAccessToken()).isEqualTo(ACCESS_TOKEN);
			assertThat(tokens.getRefreshToken()).isPresent();
			assertThat(tokens.getActiveUntil())
					.isEqualTo(SESSION.getActiveUntil());
		}

		@Test
		void loginNull() {
			assertThatThrownBy(() -> sessionService.create(null))
					.isInstanceOf(NullPointerException.class);
		}

		@Test
		void userNotFound() {
			final UserLogin login = USER.login();
			when(userService.findByLogin(login)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> sessionService.create(login))
					.isInstanceOf(MissingEntityException.class);
		}
	}

	@Nested
	class Update {

		private static final TestOrganisationUser ORGANISATION_USER = TestOrganisationUser.SPORER_EDUARDO_CHRISTIANSEN;
		private static final TestOrganisation ORGANISATION = ORGANISATION_USER
				.getOrganisation();

		private static final Principal PRINCIPAL = SESSION.principal();
		private static final SessionUpdate UPDATE = new SessionUpdate(
				SESSION.getId(), ORGANISATION.getId());

		@Test
		void success() {
			final SessionInfo bdo = mock();
			when(bdo.getUserId()).thenReturn(SESSION.getUser().getId());

			when(organisationUserService.getAll(PRINCIPAL))
					.thenReturn(List.of(ORGANISATION_USER.info()));

			when(sessionStorageGateway.findById(SESSION.getId()))
					.thenReturn(Optional.of(bdo));

			final SessionInfo updated = new SessionInfo(SESSION.getId(),
					USER.getId(), ORGANISATION.getId(),
					SESSION.getActiveUntil());
			when(sessionStorageGateway.update(bdo)).thenReturn(updated);
			when(accessTokenService.create(updated)).thenReturn(ACCESS_TOKEN);

			final SessionTokens result = sessionService.update(PRINCIPAL,
					UPDATE);

			assertThat(result.getId()).isEqualTo(SESSION.getId());
			assertThat(result.getAccessToken()).isEqualTo(ACCESS_TOKEN);
			assertThat(result.getRefreshToken()).isEmpty();
			assertThat(result.getActiveUntil())
					.isEqualTo(SESSION.getActiveUntil());

			final InOrder order = inOrder(bdo, sessionStorageGateway,
					accessTokenService);
			order.verify(bdo).update(UPDATE);
			order.verify(sessionStorageGateway).update(bdo);
			order.verify(accessTokenService).create(updated);
		}

		@Test
		void sessionNotFound() {
			when(organisationUserService.getAll(PRINCIPAL))
					.thenReturn(List.of(ORGANISATION_USER.info()));

			when(sessionStorageGateway.findById(SESSION.getId()))
					.thenReturn(Optional.empty());

			assertThatThrownBy(() -> sessionService.update(PRINCIPAL, UPDATE))
					.isInstanceOf(MissingEntityException.class);
		}

		@Test
		void mismatchedUser() {
			final SessionInfo bdo = mock();
			when(bdo.getUserId()).thenReturn(SESSION.getUser().getId() + 1);

			when(organisationUserService.getAll(PRINCIPAL))
					.thenReturn(List.of(ORGANISATION_USER.info()));

			when(sessionStorageGateway.findById(SESSION.getId()))
					.thenReturn(Optional.of(bdo));

			assertThatThrownBy(() -> sessionService.update(PRINCIPAL, UPDATE))
					.isInstanceOf(MissingEntityException.class);
		}

		@Test
		void organisationNotFound() {
			when(organisationUserService.getAll(PRINCIPAL))
					.thenReturn(Collections.emptyList());

			when(sessionStorageGateway.findById(SESSION.getId()))
					.thenReturn(Optional.of(SESSION.info()));

			final var found = new OrganisationUserInfo(Long.MAX_VALUE,
					USER.getId());
			when(organisationUserService.getAll(PRINCIPAL))
					.thenReturn(List.of(found));

			assertThatThrownBy(() -> sessionService.update(PRINCIPAL, UPDATE))
					.isInstanceOf(MissingEntityException.class);
		}

		@Test
		void principalNull() {
			assertThatThrownBy(() -> sessionService.update(null, UPDATE))
					.isInstanceOf(NullPointerException.class);

			verifyNoInteractions(sessionStorageGateway);
			verifyNoInteractions(accessTokenService);
		}

		@Test
		void updateNull() {
			assertThatThrownBy(() -> sessionService.update(PRINCIPAL, null))
					.isInstanceOf(NullPointerException.class);

			verifyNoInteractions(sessionStorageGateway);
			verifyNoInteractions(accessTokenService);
		}
	}
}
