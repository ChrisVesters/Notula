package com.cvesters.notula.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
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
			when(sessionStorageGateway.create(argThat(bdo -> {
				assertThatThrownBy(() -> bdo.getId())
						.isInstanceOf(IllegalStateException.class);
				assertThat(bdo.getUserId()).isEqualTo(USER.getId());
				assertThat(bdo.getOrganisationId()).isEmpty();
				assertThat(bdo.getActiveUntil()).isNotNull();
				return true;
			}), anyString())).thenReturn(createdSession);

			when(accessTokenService.create(createdSession))
					.thenReturn(ACCESS_TOKEN);

			final SessionTokens tokens = sessionService.create(login);

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
				ORGANISATION.getId());

		@Test
		void success() {
			final long sessionId = SESSION.getId();
			final SessionInfo bdo = mock();
			when(bdo.getUserId()).thenReturn(SESSION.getUser().getId());

			when(organisationUserService.getAll(PRINCIPAL))
					.thenReturn(List.of(ORGANISATION_USER.info()));

			when(sessionStorageGateway.findById(sessionId))
					.thenReturn(Optional.of(bdo));

			final SessionInfo updated = new SessionInfo(sessionId, USER.getId(),
					ORGANISATION.getId(), SESSION.getActiveUntil());
			when(sessionStorageGateway.update(bdo)).thenReturn(updated);
			when(accessTokenService.create(updated)).thenReturn(ACCESS_TOKEN);

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
			order.verify(accessTokenService).create(updated);
		}

		@Test
		void sessionNotFound() {
			final long sessionId = SESSION.getId();
			when(organisationUserService.getAll(PRINCIPAL))
					.thenReturn(List.of(ORGANISATION_USER.info()));

			when(sessionStorageGateway.findById(sessionId))
					.thenReturn(Optional.empty());

			assertThatThrownBy(
					() -> sessionService.update(PRINCIPAL, sessionId, UPDATE))
							.isInstanceOf(MissingEntityException.class);
		}

		@Test
		void mismatchedUser() {
			final long sessionId = SESSION.getId();
			final SessionInfo bdo = mock();
			when(bdo.getUserId()).thenReturn(SESSION.getUser().getId() + 1);

			when(organisationUserService.getAll(PRINCIPAL))
					.thenReturn(List.of(ORGANISATION_USER.info()));

			when(sessionStorageGateway.findById(SESSION.getId()))
					.thenReturn(Optional.of(bdo));

			assertThatThrownBy(
					() -> sessionService.update(PRINCIPAL, sessionId, UPDATE))
							.isInstanceOf(MissingEntityException.class);
		}

		@Test
		void organisationNotFound() {
			final long sessionId = SESSION.getId();
			when(organisationUserService.getAll(PRINCIPAL))
					.thenReturn(Collections.emptyList());

			when(sessionStorageGateway.findById(SESSION.getId()))
					.thenReturn(Optional.of(SESSION.info()));

			final var found = new OrganisationUserInfo(Long.MAX_VALUE,
					USER.getId());
			when(organisationUserService.getAll(PRINCIPAL))
					.thenReturn(List.of(found));

			assertThatThrownBy(
					() -> sessionService.update(PRINCIPAL, sessionId, UPDATE))
							.isInstanceOf(MissingEntityException.class);
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
		void success() {
			final long sessionId = SESSION.getId();
			final String refreshToken = SESSION.getRefreshToken();

			final SessionInfo bdo = mock();
			when(sessionStorageGateway.findByIdAndRefreshToken(sessionId,
					refreshToken)).thenReturn(Optional.of(bdo));

			final SessionInfo updated = new SessionInfo(SESSION.getId(),
					USER.getId(), ORGANISATION.getId(),
					SESSION.getActiveUntil());
			when(sessionStorageGateway.update(eq(bdo), anyString()))
					.thenReturn(updated);
			when(accessTokenService.create(updated)).thenReturn(ACCESS_TOKEN);

			final SessionTokens result = sessionService.refresh(SESSION.getId(),
					SESSION.getRefreshToken());

			final var newToken = ArgumentCaptor.forClass(String.class);

			final InOrder order = inOrder(bdo, sessionStorageGateway,
					accessTokenService);
			order.verify(bdo).refresh();
			order.verify(sessionStorageGateway)
					.update(eq(bdo), newToken.capture());
			order.verify(accessTokenService).create(updated);

			assertThat(result.getId()).isEqualTo(SESSION.getId());
			assertThat(result.getAccessToken()).isEqualTo(ACCESS_TOKEN);
			assertThat(result.getRefreshToken()).contains(newToken.getValue());
			assertThat(result.getActiveUntil())
					.isEqualTo(SESSION.getActiveUntil());

		}

		@Test
		void sessionNotFound() {
			final long sessionId = SESSION.getId();
			final String refreshToken = SESSION.getRefreshToken();

			when(organisationUserService.getAll(PRINCIPAL))
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
	}

}
