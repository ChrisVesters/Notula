package com.cvesters.notula.session;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.user.UserService;

class SessionServiceTest {

	private final UserService userService = mock();
	private final AccessTokenService jwtService = mock();
	private final SessionStorageGateway sessionStorageGateway = mock();

	private final SessionService sessionService = new SessionService(
			userService, jwtService, sessionStorageGateway);

	@Nested
	class Create {

		@Test
		void loginNull() {
			assertThatThrownBy(() -> sessionService.create(null))
					.isInstanceOf(NullPointerException.class);
		}

		// null request -> NPE
		// email does not exist -> BADREQUEST
		// password incorrects -> BADREQUEST
		// success -> created session and returns tokens

	}
}
