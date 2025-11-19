package com.cvesters.notula.session.dao;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.cvesters.notula.session.TestSession;

class SessionDaoTest {

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_DEKSTOP;
	private final SessionDao dao = new SessionDao(SESSION.getUser().getId(),
			SESSION.getRefreshToken(), SESSION.getActiveUntil());

	@Test
	void toBdo() throws Exception {
		final var idField = dao.getClass().getDeclaredField("id");
		idField.setAccessible(true);
		idField.set(dao, SESSION.getId());

		final var bdo = dao.toBdo();

		assertThat(bdo.getId()).isEqualTo(SESSION.getId());
		assertThat(bdo.getUserId()).isEqualTo(SESSION.getUser().getId());
		assertThat(bdo.getRefreshToken()).isEqualTo(SESSION.getRefreshToken());
		assertThat(bdo.getActiveUntil()).isEqualTo(SESSION.getActiveUntil());
	}
}
