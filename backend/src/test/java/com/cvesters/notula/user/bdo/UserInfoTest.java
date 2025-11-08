package com.cvesters.notula.user.bdo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.common.domain.Email;
import com.cvesters.notula.user.TestUser;

class UserInfoTest {

	private static final TestUser USER = TestUser.EDUARDO_CHRISTIANSEN;

	@Nested
	class Constructor {

		@Test
		void success() {
			final long userId = USER.getId();
			final Email email = USER.getEmail();

			final var userInfo = new UserInfo(userId, email);

			assertThat(userInfo.getId()).isEqualTo(userId);
			assertThat(userInfo.getEmail()).isEqualTo(email);
		}

		@Test
		void emailNull() {
			final long userId = USER.getId();
			final Email email = null;

			assertThatThrownBy(() -> new UserInfo(userId, email))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
