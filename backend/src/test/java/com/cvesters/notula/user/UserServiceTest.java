package com.cvesters.notula.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.common.domain.Email;
import com.cvesters.notula.common.exception.DuplicateEntityException;
import com.cvesters.notula.user.bdo.UserInfo;

class UserServiceTest {

	private static final TestUser USER = TestUser.EDUARDO_CHRISTIANSEN;

	private final UserStorageGateway userStorageGateway = mock();
	private final UserService userService = new UserService(userStorageGateway);

	@Nested
	class Create {

		@Test
		void success() {
			final UserInfo userInfo = USER.info();

			when(userStorageGateway.existsByEmail(userInfo.getEmail()))
					.thenReturn(false);
			when(userStorageGateway.create(userInfo)).thenReturn(userInfo);

			final UserInfo info = userService.createUser(userInfo);

			assertThat(info).isEqualTo(userInfo);
		}

		@Test
		void userInfoNull() {
			assertThatThrownBy(() -> userService.createUser((UserInfo) null))
					.isInstanceOf(NullPointerException.class);

			verifyNoInteractions(userStorageGateway);
		}

		@Test
		void emailAlreadyUsed() {
			final UserInfo userInfo = USER.info();

			when(userStorageGateway.existsByEmail(userInfo.getEmail()))
					.thenReturn(true);

			assertThatThrownBy(() -> userService.createUser(userInfo))
					.isInstanceOf(DuplicateEntityException.class);
		}
	}

	@Nested
	class FindByEmail {

		@Test
		void success() {
			final Email email = USER.getEmail();
			final Optional<UserInfo> result = Optional.of(USER.info());
			when(userStorageGateway.findByEmail(email)).thenReturn(result);

			final Optional<UserInfo> foundUser = userService.findByEmail(email);

			assertThat(foundUser).isEqualTo(result);
		}

		@Test
		void emailNull() {
			assertThatThrownBy(() -> userService.findByEmail(null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
