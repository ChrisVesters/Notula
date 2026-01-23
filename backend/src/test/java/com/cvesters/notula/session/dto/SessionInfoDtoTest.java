package com.cvesters.notula.session.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.session.TestSession;
import com.cvesters.notula.session.bdo.SessionTokens;

class SessionInfoDtoTest {

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
	private static final String ACCESS_TOKEN = "access";

	@Nested
	class Constructor {

		@Test
		void success() {
			final var bdo = new SessionTokens(SESSION.info(), ACCESS_TOKEN,
					SESSION.getRefreshToken());

			final var dto = new SessionInfoDto(bdo);

			assertThat(dto.id()).isEqualTo(SESSION.getId());
			assertThat(dto.accessToken()).isEqualTo(ACCESS_TOKEN);
			assertThat(OffsetDateTime.parse(dto.activeUntil()))
					.isEqualTo(SESSION.getActiveUntil());
		}

		@Test
		void sessionTokensNull() {
			assertThatThrownBy(() -> new SessionInfoDto(null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
