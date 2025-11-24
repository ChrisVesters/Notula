package com.cvesters.notula.session.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.session.TestSession;

class SessionDaoTest {

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_DEKSTOP;

	@Nested
	class Constructor {

		@Test
		void success() {
			final var bdo = SESSION.info();

			final var dao = new SessionDao(bdo);

			assertThat(dao.getId()).isNull();
			assertThat(dao.getUserId()).isEqualTo(bdo.getUserId());
			assertThat(dao.getRefreshToken()).isEqualTo(bdo.getRefreshToken());
			assertThat(dao.getActiveUntil()).isEqualTo(bdo.getActiveUntil());
		}

		@Test
		void bdoNull() {
			assertThatThrownBy(() -> new SessionDao(null))
					.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class ToBdo {

		private final SessionDao dao = new SessionDao(SESSION.info());

		@Test
		void success() throws Exception {
			final var idField = dao.getClass().getDeclaredField("id");
			idField.setAccessible(true);
			idField.set(dao, SESSION.getId());

			final var bdo = dao.toBdo();

			assertThat(bdo.getId()).isEqualTo(SESSION.getId());
			assertThat(bdo.getUserId()).isEqualTo(SESSION.getUser().getId());
			assertThat(bdo.getRefreshToken())
					.isEqualTo(SESSION.getRefreshToken());
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
