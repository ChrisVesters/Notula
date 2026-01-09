package com.cvesters.notula.session.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.session.TestSession;
import com.cvesters.notula.session.bdo.SessionCreateAction;

class SessionDaoTest {

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_DEKSTOP;
	private static final String HASHED_REFRESH_TOKEN = "hash";

	@Nested
	class Constructor {

		@Test
		void success() {
			final var userInfo = SESSION.getUser().info();
			final var bdo = new SessionCreateAction(userInfo);

			final var dao = new SessionDao(bdo, HASHED_REFRESH_TOKEN);

			assertThat(dao.getId()).isNull();
			assertThat(dao.getUserId()).isEqualTo(bdo.getUserId());
			assertThat(dao.getRefreshToken()).isEqualTo(HASHED_REFRESH_TOKEN);
			assertThat(dao.getActiveUntil()).isEqualTo(bdo.getActiveUntil());
		}

		@Test
		void bdoNull() {
			assertThatThrownBy(() -> new SessionDao(null, HASHED_REFRESH_TOKEN))
					.isInstanceOf(NullPointerException.class);
		}

		@Test
		void refreshTokenNull() {
			final var userInfo = SESSION.getUser().info();
			final var bdo = new SessionCreateAction(userInfo);

			assertThatThrownBy(() -> new SessionDao(bdo, null))
					.isInstanceOf(NullPointerException.class);
		}

	}

	@Nested
	class ToBdo {

		private final SessionCreateAction bdo = mock();
		private SessionDao dao;

		@BeforeEach
		void setup() {
			when(bdo.getUserId()).thenReturn(SESSION.getUser().getId());
			when(bdo.getActiveUntil()).thenReturn(SESSION.getActiveUntil());
			
			dao = new SessionDao(bdo, HASHED_REFRESH_TOKEN);
		}


		@Test
		void success() throws Exception {
			final var idField = dao.getClass().getDeclaredField("id");
			idField.setAccessible(true);
			idField.set(dao, SESSION.getId());

			final var bdo = dao.toBdo();

			assertThat(bdo.getId()).isEqualTo(SESSION.getId());
			assertThat(bdo.getUserId()).isEqualTo(SESSION.getUser().getId());
			assertThat(bdo.getActiveUntil())
					.isEqualTo(SESSION.getActiveUntil());
		}

		@Test
		void idNull() {
			assertThatThrownBy(dao::toBdo)
					.isInstanceOf(IllegalStateException.class);
		}
	}
}
