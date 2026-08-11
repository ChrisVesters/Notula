package com.cvesters.notula.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.common.exception.MissingEntityException;
import com.cvesters.notula.credential.CredentialActionMatcher;
import com.cvesters.notula.credential.CredentialService;
import com.cvesters.notula.credential.TestCredential;
import com.cvesters.notula.credential.bdo.CredentialAction;
import com.cvesters.notula.organisation.OrganisationUserService;
import com.cvesters.notula.organisation.TestOrganisation;
import com.cvesters.notula.organisation.TestOrganisationUser;
import com.cvesters.notula.session.bdo.SessionAction;
import com.cvesters.notula.session.bdo.SessionInfo;
import com.cvesters.notula.session.bdo.SessionTokens;
import com.cvesters.notula.session.bdo.SessionUpdate;
import com.cvesters.notula.user.TestUser;
import com.cvesters.notula.user.UserService;
import com.cvesters.notula.user.bdo.UserInfo;

class SessionServiceTest {

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
	private static final TestUser USER = SESSION.getUser();
	private static final TestCredential CREDENTIAL = TestCredential.EDUARDO_CHRISTIANSEN;
	private static final String ACCESS_TOKEN = "access";

	private final UserService userService = mock();
	private final CredentialService credentialService = mock();
	private final OrganisationUserService organisationUserService = mock();
	private final AccessTokenService accessTokenService = mock();
	private final SessionStorageGateway sessionStorageGateway = mock();

	private final SessionService sessionService = new SessionService(
			userService, credentialService, organisationUserService,
			accessTokenService, sessionStorageGateway);

	@Nested
	class Create {

		@Test
		void success() {
			final var request = new SessionAction.Create(USER.getEmail(),
					CREDENTIAL.getPassword());
			final UserInfo userInfo = USER.info();
			when(userService.findByEmail(USER.getEmail()))
					.thenReturn(Optional.of(userInfo));

			final CredentialAction.Login login = CREDENTIAL.login();
			final var matcher = new CredentialActionMatcher.Login(login);
			when(credentialService.existsLogin(argThat(matcher::matches)))
					.thenReturn(true);

			when(organisationUserService.getAllForUser(argThat(principal -> {
				assertThat(principal.userId()).isEqualTo(USER.getId());
				return true;
			}))).thenReturn(Collections.emptyList());

			final SessionInfo createdSession = SESSION.info();
			when(sessionStorageGateway.create(argThat(bdo -> {
				assertThatThrownBy(bdo::getId)
						.isInstanceOf(IllegalStateException.class);
				assertThat(bdo.getUserId()).isEqualTo(USER.getId());
				assertThat(bdo.getOrganisationId()).isEmpty();
				assertThat(bdo.getActiveUntil()).isNotNull();
				return true;
			}), anyString())).thenReturn(createdSession);

			when(accessTokenService.create(createdSession, null))
					.thenReturn(ACCESS_TOKEN);

			final SessionTokens tokens = sessionService.create(request);

			final var refreshToken = ArgumentCaptor.forClass(String.class);
			verify(sessionStorageGateway).create(any(), refreshToken.capture());

			assertThat(tokens.getId()).isEqualTo(SESSION.getId());
			assertThat(tokens.getAccessToken()).isEqualTo(ACCESS_TOKEN);
			assertThat(tokens.getRefreshToken())
					.contains(refreshToken.getValue());
			assertThat(tokens.getActiveUntil())
					.isEqualTo(SESSION.getActiveUntil());
		}

		@Test
		void withDefaultOrg() {
			final var request = new SessionAction.Create(USER.getEmail(),
					CREDENTIAL.getPassword());
			final UserInfo userInfo = USER.info();
			when(userService.findByEmail(USER.getEmail()))
					.thenReturn(Optional.of(userInfo));

			final CredentialAction.Login login = CREDENTIAL.login();
			final var matcher = new CredentialActionMatcher.Login(login);
			when(credentialService.existsLogin(argThat(matcher::matches)))
					.thenReturn(true);

			final TestOrganisationUser organisationUser = TestOrganisationUser.SPORER_EDUARDO_CHRISTIANSEN;
			when(organisationUserService.getAllForUser(argThat(principal -> {
				assertThat(principal.userId()).isEqualTo(USER.getId());
				return true;
			}))).thenReturn(List.of(organisationUser.info()));

			final SessionInfo createdSession = SESSION.info();
			when(sessionStorageGateway.create(argThat(bdo -> {
				assertThatThrownBy(bdo::getId)
						.isInstanceOf(IllegalStateException.class);
				assertThat(bdo.getUserId()).isEqualTo(USER.getId());
				assertThat(bdo.getOrganisationId())
						.contains(organisationUser.getOrganisation().getId());
				assertThat(bdo.getActiveUntil()).isNotNull();
				return true;
			}), anyString())).thenReturn(createdSession);

			when(accessTokenService.create(createdSession,
					organisationUser.getRole())).thenReturn(ACCESS_TOKEN);

			final SessionTokens tokens = sessionService.create(request);

			final var refreshToken = ArgumentCaptor.forClass(String.class);
			verify(sessionStorageGateway).create(any(), refreshToken.capture());

			assertThat(tokens.getId()).isEqualTo(SESSION.getId());
			assertThat(tokens.getAccessToken()).isEqualTo(ACCESS_TOKEN);
			assertThat(tokens.getRefreshToken())
					.contains(refreshToken.getValue());
			assertThat(tokens.getActiveUntil())
					.isEqualTo(SESSION.getActiveUntil());
		}

		@Test
		void multipleOrgs() {
			final var request = new SessionAction.Create(USER.getEmail(),
					CREDENTIAL.getPassword());
			final UserInfo userInfo = USER.info();
			when(userService.findByEmail(USER.getEmail()))
					.thenReturn(Optional.of(userInfo));

			final CredentialAction.Login login = CREDENTIAL.login();
			final var matcher = new CredentialActionMatcher.Login(login);
			when(credentialService.existsLogin(argThat(matcher::matches)))
					.thenReturn(true);

			when(organisationUserService.getAllForUser(argThat(principal -> {
				assertThat(principal.userId()).isEqualTo(USER.getId());
				return true;
			}))).thenReturn(
					List.of(TestOrganisationUser.HEUL_ALISON_DACH.info(),
							TestOrganisationUser.GLOVER_ALISON_DACH.info()));

			final SessionInfo createdSession = SESSION.info();
			when(sessionStorageGateway.create(argThat(bdo -> {
				assertThatThrownBy(bdo::getId)
						.isInstanceOf(IllegalStateException.class);
				assertThat(bdo.getUserId()).isEqualTo(USER.getId());
				assertThat(bdo.getOrganisationId()).isEmpty();
				assertThat(bdo.getActiveUntil()).isNotNull();
				return true;
			}), anyString())).thenReturn(createdSession);

			when(accessTokenService.create(createdSession, null))
					.thenReturn(ACCESS_TOKEN);

			final SessionTokens tokens = sessionService.create(request);

			final var refreshToken = ArgumentCaptor.forClass(String.class);
			verify(sessionStorageGateway).create(any(), refreshToken.capture());

			assertThat(tokens.getId()).isEqualTo(SESSION.getId());
			assertThat(tokens.getAccessToken()).isEqualTo(ACCESS_TOKEN);
			assertThat(tokens.getRefreshToken())
					.contains(refreshToken.getValue());
			assertThat(tokens.getActiveUntil())
					.isEqualTo(SESSION.getActiveUntil());
		}

		@Test
		void requestNull() {
			assertThatThrownBy(() -> sessionService.create(null))
					.isInstanceOf(NullPointerException.class);
		}

		@Test
		void userNotFound() {
			final var request = new SessionAction.Create(USER.getEmail(),
					CREDENTIAL.getPassword());
			when(userService.findByEmail(USER.getEmail()))
					.thenReturn(Optional.empty());

			assertThatThrownBy(() -> sessionService.create(request))
					.isInstanceOf(MissingEntityException.class);
		}

		@Test
		void invalidCredentials() {
			final var request = new SessionAction.Create(USER.getEmail(),
					CREDENTIAL.getPassword());
			final UserInfo userInfo = USER.info();
			when(userService.findByEmail(USER.getEmail()))
					.thenReturn(Optional.of(userInfo));

			when(credentialService.existsLogin(new CredentialAction.Login(
					USER.getId(), CREDENTIAL.getPassword()))).thenReturn(false);

			assertThatThrownBy(() -> sessionService.create(request))
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
				ORGANISATION.getId());

		@Test
		void success() {
			final long sessionId = SESSION.getId();
			final SessionInfo bdo = mock();
			when(bdo.getUserId()).thenReturn(SESSION.getUser().getId());
			when(bdo.isActive()).thenReturn(true);

			when(organisationUserService.getAllForUser(PRINCIPAL))
					.thenReturn(List.of(ORGANISATION_USER.info()));

			when(sessionStorageGateway.findById(sessionId))
					.thenReturn(Optional.of(bdo));

			final SessionInfo updated = new SessionInfo(sessionId, USER.getId(),
					ORGANISATION.getId(), SESSION.getActiveUntil());
			when(sessionStorageGateway.update(bdo)).thenReturn(updated);
			when(accessTokenService.create(updated,
					ORGANISATION_USER.getRole())).thenReturn(ACCESS_TOKEN);

			final SessionTokens result = sessionService.update(PRINCIPAL,
					sessionId, UPDATE);

			assertThat(result.getId()).isEqualTo(SESSION.getId());
			assertThat(result.getAccessToken()).isEqualTo(ACCESS_TOKEN);
			assertThat(result.getRefreshToken()).isEmpty();
			assertThat(result.getActiveUntil())
					.isEqualTo(SESSION.getActiveUntil());

			final InOrder order = inOrder(bdo, sessionStorageGateway,
					accessTokenService);
			order.verify(bdo).update(UPDATE);
			order.verify(sessionStorageGateway).update(bdo);
			order.verify(accessTokenService)
					.create(updated, ORGANISATION_USER.getRole());
		}

		@Test
		void sessionNotFound() {
			final long sessionId = SESSION.getId();
			when(organisationUserService.getAllForUser(PRINCIPAL))
					.thenReturn(List.of(ORGANISATION_USER.info()));

			when(sessionStorageGateway.findById(sessionId))
					.thenReturn(Optional.empty());

			assertThatThrownBy(
					() -> sessionService.update(PRINCIPAL, sessionId, UPDATE))
							.isInstanceOf(MissingEntityException.class);

			verify(sessionStorageGateway, never()).update(any());
			verifyNoInteractions(accessTokenService);
		}

		@Test
		void sessionNotActive() {
			final long sessionId = SESSION.getId();
			final SessionInfo bdo = mock();
			when(bdo.getUserId()).thenReturn(SESSION.getUser().getId());
			when(bdo.isActive()).thenReturn(false);

			when(organisationUserService.getAllForUser(PRINCIPAL))
					.thenReturn(List.of(ORGANISATION_USER.info()));

			when(sessionStorageGateway.findById(sessionId))
					.thenReturn(Optional.of(bdo));

			assertThatThrownBy(
					() -> sessionService.update(PRINCIPAL, sessionId, UPDATE))
							.isInstanceOf(MissingEntityException.class);

			verify(sessionStorageGateway, never()).update(any());
			verifyNoInteractions(accessTokenService);
		}

		@Test
		void mismatchedUser() {
			final long sessionId = SESSION.getId();
			final SessionInfo bdo = mock();
			when(bdo.getUserId()).thenReturn(SESSION.getUser().getId() + 1);
			when(bdo.isActive()).thenReturn(true);

			when(organisationUserService.getAllForUser(PRINCIPAL))
					.thenReturn(List.of(ORGANISATION_USER.info()));

			when(sessionStorageGateway.findById(SESSION.getId()))
					.thenReturn(Optional.of(bdo));

			assertThatThrownBy(
					() -> sessionService.update(PRINCIPAL, sessionId, UPDATE))
							.isInstanceOf(MissingEntityException.class);

			verify(sessionStorageGateway, never()).update(any());
			verifyNoInteractions(accessTokenService);
		}

		@Test
		void organisationUsersNotFound() {
			final long sessionId = SESSION.getId();
			when(organisationUserService.getAllForUser(PRINCIPAL))
					.thenReturn(Collections.emptyList());

			assertThatThrownBy(
					() -> sessionService.update(PRINCIPAL, sessionId, UPDATE))
							.isInstanceOf(MissingEntityException.class);

			verifyNoInteractions(sessionStorageGateway);
			verifyNoInteractions(accessTokenService);
		}

		@Test
		void organisationNotFound() {
			final long sessionId = SESSION.getId();
			when(organisationUserService.getAllForUser(PRINCIPAL))
					.thenReturn(List.of(ORGANISATION_USER.info()));

			final SessionUpdate update = new SessionUpdate(
					ORGANISATION.getId() + 1);

			assertThatThrownBy(
					() -> sessionService.update(PRINCIPAL, sessionId, update))
							.isInstanceOf(MissingEntityException.class);

			verifyNoInteractions(sessionStorageGateway);
			verifyNoInteractions(accessTokenService);
		}

		@Test
		void principalNull() {
			final long sessionId = SESSION.getId();

			assertThatThrownBy(
					() -> sessionService.update(null, sessionId, UPDATE))
							.isInstanceOf(NullPointerException.class);

			verifyNoInteractions(sessionStorageGateway);
			verifyNoInteractions(accessTokenService);
		}

		@Test
		void updateNull() {
			final long sessionId = SESSION.getId();

			assertThatThrownBy(
					() -> sessionService.update(PRINCIPAL, sessionId, null))
							.isInstanceOf(NullPointerException.class);

			verifyNoInteractions(sessionStorageGateway);
			verifyNoInteractions(accessTokenService);
		}
	}

	@Nested
	class Refresh {

		private static final TestOrganisationUser ORGANISATION_USER = TestOrganisationUser.SPORER_EDUARDO_CHRISTIANSEN;
		private static final TestOrganisation ORGANISATION = ORGANISATION_USER
				.getOrganisation();

		private static final Principal PRINCIPAL = SESSION.principal();

		@Test
		void withoutOrganisation() {
			final long sessionId = SESSION.getId();
			final String refreshToken = SESSION.getRefreshToken();

			final SessionInfo bdo = mock();
			when(sessionStorageGateway.findByIdAndRefreshToken(sessionId,
					refreshToken)).thenReturn(Optional.of(bdo));

			when(bdo.isActive()).thenReturn(true);
			when(bdo.getOrganisationId()).thenReturn(Optional.empty());

			final SessionInfo updated = new SessionInfo(SESSION.getId(),
					USER.getId(), ORGANISATION.getId(),
					SESSION.getActiveUntil());
			when(sessionStorageGateway.update(eq(bdo), anyString()))
					.thenReturn(updated);
			when(accessTokenService.create(updated, null))
					.thenReturn(ACCESS_TOKEN);

			final SessionTokens result = sessionService.refresh(SESSION.getId(),
					SESSION.getRefreshToken());

			final var newToken = ArgumentCaptor.forClass(String.class);

			final InOrder order = inOrder(bdo, sessionStorageGateway,
					accessTokenService);
			order.verify(bdo).refresh();
			order.verify(sessionStorageGateway)
					.update(eq(bdo), newToken.capture());
			order.verify(accessTokenService).create(updated, null);

			verifyNoInteractions(organisationUserService);

			assertThat(result.getId()).isEqualTo(SESSION.getId());
			assertThat(result.getAccessToken()).isEqualTo(ACCESS_TOKEN);
			assertThat(result.getRefreshToken()).contains(newToken.getValue());
			assertThat(result.getActiveUntil())
					.isEqualTo(SESSION.getActiveUntil());

			assertThat(newToken.getValue()).hasSize(86)
					.satisfies(v -> assertThat(v.chars().distinct().count())
							.isGreaterThanOrEqualTo(16));
		}

		@Test
		void withOrganisation() {
			final long sessionId = SESSION.getId();
			final String refreshToken = SESSION.getRefreshToken();

			final SessionInfo bdo = mock();
			when(sessionStorageGateway.findByIdAndRefreshToken(sessionId,
					refreshToken)).thenReturn(Optional.of(bdo));

			when(bdo.isActive()).thenReturn(true);
			when(bdo.getOrganisationId())
					.thenReturn(Optional.of(ORGANISATION.getId()));
			when(bdo.getUserId()).thenReturn(USER.getId());

			final var orgUser = TestOrganisationUser.SPORER_EDUARDO_CHRISTIANSEN;
			when(organisationUserService
					.findByUserIdAndOrganisationId(argThat(principal -> {
						assertThat(principal.userId()).isEqualTo(USER.getId());
						assertThat(principal.organisationId())
								.isEqualTo(ORGANISATION.getId());
						return true;
					}))).thenReturn(Optional.of(orgUser.info()));

			final SessionInfo updated = new SessionInfo(SESSION.getId(),
					USER.getId(), ORGANISATION.getId(),
					SESSION.getActiveUntil());
			when(sessionStorageGateway.update(eq(bdo), anyString()))
					.thenReturn(updated);
			when(accessTokenService.create(updated, orgUser.getRole()))
					.thenReturn(ACCESS_TOKEN);

			final SessionTokens result = sessionService.refresh(SESSION.getId(),
					SESSION.getRefreshToken());

			final var newToken = ArgumentCaptor.forClass(String.class);

			final InOrder order = inOrder(bdo, sessionStorageGateway,
					accessTokenService);
			order.verify(bdo).refresh();
			order.verify(sessionStorageGateway)
					.update(eq(bdo), newToken.capture());
			order.verify(accessTokenService).create(updated, orgUser.getRole());

			assertThat(result.getId()).isEqualTo(SESSION.getId());
			assertThat(result.getAccessToken()).isEqualTo(ACCESS_TOKEN);
			assertThat(result.getRefreshToken()).contains(newToken.getValue());
			assertThat(result.getActiveUntil())
					.isEqualTo(SESSION.getActiveUntil());

			assertThat(newToken.getValue()).hasSize(86)
					.satisfies(v -> assertThat(v.chars().distinct().count())
							.isGreaterThanOrEqualTo(16));
		}

		@Test
		void organisationUserNotFound() {
			final long sessionId = SESSION.getId();
			final String refreshToken = SESSION.getRefreshToken();

			final SessionInfo bdo = mock();
			when(sessionStorageGateway.findByIdAndRefreshToken(sessionId,
					refreshToken)).thenReturn(Optional.of(bdo));

			when(bdo.isActive()).thenReturn(true);
			when(bdo.getOrganisationId())
					.thenReturn(Optional.of(ORGANISATION.getId()));
			when(bdo.getUserId()).thenReturn(USER.getId());

			when(organisationUserService.findByUserIdAndOrganisationId(any()))
					.thenReturn(Optional.empty());

			assertThatThrownBy(
					() -> sessionService.refresh(sessionId, refreshToken))
							.isInstanceOf(MissingEntityException.class);

			verifyNoInteractions(accessTokenService);
			verify(sessionStorageGateway, never()).update(any(), anyString());
		}

		@Test
		void sessionNotFound() {
			final long sessionId = SESSION.getId();
			final String refreshToken = SESSION.getRefreshToken();

			when(organisationUserService.getAllForUser(PRINCIPAL))
					.thenReturn(List.of(ORGANISATION_USER.info()));

			when(sessionStorageGateway.findByIdAndRefreshToken(sessionId,
					refreshToken)).thenReturn(Optional.empty());

			assertThatThrownBy(
					() -> sessionService.refresh(sessionId, refreshToken))
							.isInstanceOf(MissingEntityException.class);
		}

		@Test
		void tokenNull() {
			final long sessionId = SESSION.getId();

			assertThatThrownBy(() -> sessionService.refresh(sessionId, null))
					.isInstanceOf(NullPointerException.class);

			verifyNoInteractions(sessionStorageGateway);
			verifyNoInteractions(accessTokenService);
		}

		@Test
		void sessionInactive() {
			final long sessionId = SESSION.getId();
			final String refreshToken = SESSION.getRefreshToken();

			final SessionInfo bdo = mock();
			when(sessionStorageGateway.findByIdAndRefreshToken(sessionId,
					refreshToken)).thenReturn(Optional.of(bdo));

			when(bdo.isActive()).thenReturn(false);

			assertThatThrownBy(
					() -> sessionService.refresh(sessionId, refreshToken))
							.isInstanceOf(MissingEntityException.class);
		}
	}

	@Nested
	class Delete {

		private static final TestOrganisationUser ORGANISATION_USER = TestOrganisationUser.SPORER_EDUARDO_CHRISTIANSEN;
		private static final TestOrganisation ORGANISATION = ORGANISATION_USER
				.getOrganisation();

		private static final Principal PRINCIPAL = SESSION.principal();

		@Test
		void success() {
			final long sessionId = SESSION.getId();
			final SessionInfo bdo = mock();
			when(bdo.getUserId()).thenReturn(SESSION.getUser().getId());
			when(sessionStorageGateway.findById(sessionId))
					.thenReturn(Optional.of(bdo));

			when(bdo.isActive()).thenReturn(true);

			final SessionInfo updated = new SessionInfo(sessionId, USER.getId(),
					ORGANISATION.getId(), SESSION.getActiveUntil());
			when(sessionStorageGateway.update(bdo)).thenReturn(updated);

			sessionService.delete(PRINCIPAL, sessionId);

			final InOrder order = inOrder(bdo, sessionStorageGateway);
			order.verify(bdo).invactivate();
			order.verify(sessionStorageGateway).update(bdo);
		}

		@Test
		void sessionAlreadyInactive() {
			final long sessionId = SESSION.getId();
			final SessionInfo bdo = mock();
			when(bdo.getUserId()).thenReturn(SESSION.getUser().getId());
			when(sessionStorageGateway.findById(sessionId))
					.thenReturn(Optional.of(bdo));

			when(bdo.isActive()).thenReturn(false);

			assertThatThrownBy(
					() -> sessionService.delete(PRINCIPAL, sessionId))
							.isInstanceOf(MissingEntityException.class);

			verify(bdo, never()).invactivate();
			verify(sessionStorageGateway, never()).update(bdo);
		}

		@Test
		void sessionNotFound() {
			final long sessionId = SESSION.getId();
			when(sessionStorageGateway.findById(sessionId))
					.thenReturn(Optional.empty());

			assertThatThrownBy(
					() -> sessionService.delete(PRINCIPAL, sessionId))
							.isInstanceOf(MissingEntityException.class);
		}

		@Test
		void mismatchedUser() {
			final long sessionId = SESSION.getId();
			final SessionInfo bdo = mock();
			when(bdo.getUserId()).thenReturn(SESSION.getUser().getId() + 1);
			when(bdo.isActive()).thenReturn(true);

			when(sessionStorageGateway.findById(SESSION.getId()))
					.thenReturn(Optional.of(bdo));

			assertThatThrownBy(
					() -> sessionService.delete(PRINCIPAL, sessionId))
							.isInstanceOf(MissingEntityException.class);
		}

		@Test
		void principalNull() {
			final long sessionId = SESSION.getId();

			assertThatThrownBy(() -> sessionService.delete(null, sessionId))
					.isInstanceOf(NullPointerException.class);

			verifyNoInteractions(sessionStorageGateway);
			verifyNoInteractions(accessTokenService);
		}
	}

}
