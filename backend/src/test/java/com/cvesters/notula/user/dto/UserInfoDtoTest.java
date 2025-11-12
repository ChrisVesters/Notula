package com.cvesters.notula.user.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.user.TestUser;
import com.cvesters.notula.user.bdo.UserInfo;

class UserInfoDtoTest {

	private final TestUser USER = TestUser.EDUARDO_CHRISTIANSEN;

	@Nested
	class Constructor {

		@Test
		void success() {
			final UserInfo info = USER.info();

			final var dto = new UserInfoDto(info);

			assertThat(dto.id()).isEqualTo(USER.getId());
			assertThat(dto.email()).isEqualTo(USER.getEmail().value());
		}

		@Test
		void userInfoNull() {
			assertThatThrownBy(() -> new UserInfoDto(null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
