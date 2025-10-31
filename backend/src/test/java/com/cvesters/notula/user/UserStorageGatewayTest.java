package com.cvesters.notula.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.cvesters.notula.common.domain.Email;
import com.cvesters.notula.user.bdo.UserInfo;
import com.cvesters.notula.user.dao.UserDao;

class UserStorageGatewayTest {

	private static final TestUser USER = TestUser.EDUARDO_CHRISTIANSEN;

	private final UserRepository userRepository = mock();
	private final PasswordEncoder passwordEncoder = mock();

	private final UserStorageGateway gateway = new UserStorageGateway(userRepository,
			passwordEncoder);

	@Nested
	class CreateUser {

		@Test
		void success() {
			final String hashedPassword = "hash";
			when(passwordEncoder.encode(USER.getPassword().value()))
					.thenReturn(hashedPassword);

			final UserDao created = mock();
			final UserInfo bdo = mock();
			when(created.toBdo()).thenReturn(bdo);

			when(userRepository.save(argThat(dao -> {
				assertThat(dao.getId()).isNull();
				assertThat(dao.getEmail()).isEqualTo(USER.getEmail().value());
				assertThat(dao.getPassword()).isEqualTo(hashedPassword);
				return true;
			}))).thenReturn(created);

			final UserInfo userInfo = gateway.createUser(USER.login());

			assertThat(userInfo).isEqualTo(bdo);
		}

		@Test
		void userLoginNull() {
			assertThatThrownBy(() -> gateway.createUser(null))
					.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class ExistsByEmail {

		@ParameterizedTest
		@ValueSource(booleans = { true, false })
		void found(final boolean exists) {
			final Email email = USER.getEmail();
			when(userRepository.existsByEmail(email.value())).thenReturn(exists);

			assertThat(gateway.existsByEmail(email)).isEqualTo(exists);
		}

		@Test
		void emailNull() {
			assertThatThrownBy(() -> gateway.existsByEmail(null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
