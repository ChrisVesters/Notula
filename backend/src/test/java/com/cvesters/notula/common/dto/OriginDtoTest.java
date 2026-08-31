package com.cvesters.notula.common.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.common.domain.Origin;
import com.cvesters.notula.session.TestSession;
import com.cvesters.notula.user.TestUser;

class OriginDtoTest {

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
	private static final TestUser USER = SESSION.getUser();

	private static final UUID CLIENT_ID = UUID
			.fromString("3f9c1a44-1d2e-4a51-8b0c-2c7e9b1d4a15");

	@Nested
	class Constructor {

		@Test
		void success() {
			final var origin = new Origin(SESSION.principal(), CLIENT_ID);

			final var dto = new OriginDto(origin);

			assertThat(dto.userId()).isEqualTo(USER.getId());
			assertThat(dto.clientId()).isEqualTo(CLIENT_ID);
		}

		@Test
		void withoutClientId() {
			final var origin = new Origin(SESSION.principal());

			final var dto = new OriginDto(origin);

			assertThat(dto.userId()).isEqualTo(USER.getId());
			assertThat(dto.clientId()).isNull();
		}

		@Test
		void originNull() {
			assertThatThrownBy(() -> new OriginDto(null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
