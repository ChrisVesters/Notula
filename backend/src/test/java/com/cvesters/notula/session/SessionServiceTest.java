package com.cvesters.notula.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.common.exception.MissingEntityException;
import com.cvesters.notula.session.bdo.SessionInfo;
import com.cvesters.notula.session.bdo.SessionTokens;
import com.cvesters.notula.user.TestUser;
import com.cvesters.notula.user.UserService;
import com.cvesters.notula.user.bdo.UserInfo;
import com.cvesters.notula.user.bdo.UserLogin;

class SessionServiceTest {

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_DEKSTOP;
	private static final TestUser USER = SESSION.getUser();
	private static final String ACCESS_TOKEN = "access";

	private final UserService userService = mock();
	private final AccessTokenService accessTokenService = mock();
	private final SessionStorageGateway sessionStorageGateway = mock();

	private final SessionService sessionService = new SessionService(
			userService, accessTokenService, sessionStorageGateway);

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
			assertThat(tokens.getRefreshToken())
					.isEqualTo(SESSION.getRefreshToken());
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
}
