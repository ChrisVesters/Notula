package com.cvesters.notula.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.common.exception.DuplicateEntityException;
import com.cvesters.notula.user.bdo.UserInfo;
import com.cvesters.notula.user.bdo.UserLogin;

class UserServiceTest {

	private static final TestUser USER = TestUser.EDUARDO_CHRISTIANSEN;

	private final UserStorageGateway userStorageGateway = mock();
	private final UserService userService = new UserService(userStorageGateway);

	@Nested
	class CreateUser {

		@Test
		void success() {
			final UserLogin userLogin = USER.login();
			final UserInfo userInfo = USER.info();

			when(userStorageGateway.existsByEmail(userLogin.getEmail()))
					.thenReturn(false);
			when(userStorageGateway.createUser(userLogin)).thenReturn(userInfo);

			final UserInfo info = userService.createUser(userLogin);

			assertThat(info).isEqualTo(userInfo);
		}

		@Test
		void loginNull() {
			assertThatThrownBy(() -> userService.createUser(null))
					.isInstanceOf(NullPointerException.class);

			verifyNoInteractions(userStorageGateway);
		}

		@Test
		void emailAlreadyUsed() {
			final UserLogin userLogin = USER.login();

			when(userStorageGateway.existsByEmail(userLogin.getEmail()))
					.thenReturn(true);

			assertThatThrownBy(() -> userService.createUser(userLogin))
					.isInstanceOf(DuplicateEntityException.class);
		}
	}
}
