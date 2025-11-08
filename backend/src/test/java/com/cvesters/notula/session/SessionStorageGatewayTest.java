package com.cvesters.notula.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.session.bdo.SessionInfo;
import com.cvesters.notula.session.dao.SessionDao;

class SessionStorageGatewayTest {

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_DEKSTOP;

	private final SessionRepository sessionRepository = mock();

	private final SessionStorageGateway gateway = new SessionStorageGateway(
			sessionRepository);

	@Nested
	class Create {

		@Test
		void success() {
			final SessionDao created = mock();
			final SessionInfo bdo = mock();
			when(created.toBdo()).thenReturn(bdo);

			when(sessionRepository.save(argThat(dao -> {
				assertThat(dao.getId()).isNull();
				assertThat(dao.getUserId())
						.isEqualTo(SESSION.getUser().getId());
				assertThat(dao.getRefreshToken())
						.isEqualTo(SESSION.getRefreshToken());
				assertThat(dao.getActiveUntil())
						.isEqualTo(SESSION.getActiveUntil());
				return true;
			}))).thenReturn(created);

			final SessionInfo sessionInfo = gateway.create(SESSION.info());

			assertThat(sessionInfo).isEqualTo(bdo);
		}

		@Test
		void sessionInfoNull() {
			assertThatThrownBy(() -> gateway.create(null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
