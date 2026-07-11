package com.cvesters.notula.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.cvesters.notula.common.domain.Email;
import com.cvesters.notula.user.bdo.UserInfo;
import com.cvesters.notula.user.dao.UserDao;

class UserStorageGatewayTest {

	private static final TestUser USER = TestUser.EDUARDO_CHRISTIANSEN;

	private final UserRepository userRepository = mock();

	private final UserStorageGateway gateway = new UserStorageGateway(
			userRepository);

	@Nested
	class Create {

		@Test
		void success() {
			final UserInfo info = USER.info();
			final UserDao created = mock();
			final UserInfo bdo = mock();
			when(created.toBdo()).thenReturn(bdo);

			when(userRepository.save(argThat(dao -> {
				assertThat(dao.getId()).isNull();
				assertThat(dao.getEmail()).isEqualTo(USER.getEmail().value());
				return true;
			}))).thenReturn(created);

			final UserInfo userInfo = gateway.create(info);

			assertThat(userInfo).isEqualTo(bdo);
		}

		@Test
		void userInfoNull() {
			assertThatThrownBy(() -> gateway.create((UserInfo) null))
					.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class FindByEmail {

		@Test
		void found() {
			final UserInfo info = USER.info();
			final UserDao dao = mock();
			when(dao.toBdo()).thenReturn(info);

			final Email email = USER.getEmail();
			when(userRepository.findByEmail(email.value()))
					.thenReturn(Optional.of(dao));

			assertThat(gateway.findByEmail(email)).contains(info);
		}

		@Test
		void notFound() {
			final Email email = USER.getEmail();
			when(userRepository.findByEmail(email.value()))
					.thenReturn(Optional.empty());

			assertThat(gateway.findByEmail(email)).isEmpty();
		}

		@Test
		void emailNull() {
			assertThatThrownBy(() -> gateway.findByEmail(null))
					.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class ExistsByEmail {

		@ParameterizedTest
		@ValueSource(booleans = { true, false })
		void found(final boolean exists) {
			final Email email = USER.getEmail();
			when(userRepository.existsByEmail(email.value()))
					.thenReturn(exists);

			assertThat(gateway.existsByEmail(email)).isEqualTo(exists);
		}

		@Test
		void emailNull() {
			assertThatThrownBy(() -> gateway.existsByEmail(null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
