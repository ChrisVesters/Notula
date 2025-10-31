package com.cvesters.notula.session;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.cvesters.notula.user.UserService;

class SessionServiceTest {

	@MockitoBean
	private UserService userService;

	@MockitoBean
	private JwtService jwtService;

	@MockitoBean
	private SessionStorageGateway sessionStorageGateway;

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
