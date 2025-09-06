package com.cvesters.notula.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.cvesters.notula.user.bdo.UserInfo;
import com.cvesters.notula.user.dao.UserDao;

class UserStorageServiceTest {

	private UserRepository userRepository = mock();
	private PasswordEncoder passwordEncoder = mock();

	private UserStorageService service = new UserStorageService(userRepository,
			passwordEncoder);

	@Nested
	class CreateUser {

		@Test
		void success() {
			final String hashedPassword = "hash";
			final var login = TestUser.EDUARDO_CHRISTIANSEN.login();
			when(passwordEncoder.encode(login.getPassword()))
					.thenReturn(hashedPassword);

			final var created = mock(UserDao.class);
			final var bdo = mock(UserInfo.class);
			when(created.toBdo()).thenReturn(bdo);

			when(userRepository.save(argThat(dao -> {
				assertThat(dao.getEmail()).isEqualTo(login.getEmail());
				assertThat(dao.getPassword()).isEqualTo(hashedPassword);
				return true;
			}))).thenReturn(created);

			final UserInfo userInfo = service.createUser(login);

			assertThat(userInfo).isEqualTo(bdo);
		}

		@Test
		void userLoginNull() {
			assertThatThrownBy(() -> service.createUser(null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
