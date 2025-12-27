package com.cvesters.notula.session;

import java.time.OffsetDateTime;
import java.time.Period;

import lombok.Getter;

import com.cvesters.notula.session.bdo.SessionInfo;
import com.cvesters.notula.user.TestUser;

@Getter
public enum TestSession {
	EDUARDO_CHRISTIANSEN_DEKSTOP(1L, TestUser.EDUARDO_CHRISTIANSEN, "abc7775",
			OffsetDateTime.now().plus(Period.ofDays(7))),
	EDUARDO_CHRISTIANSEN_MOBILE(7L, TestUser.EDUARDO_CHRISTIANSEN, "ddef741",
			OffsetDateTime.now().minus(Period.ofDays(1)));

	private final long id;
	private final TestUser user;
	private final String refreshToken;
	private final OffsetDateTime activeUntil;

	TestSession(final long id, final TestUser user, final String refreshToken,
			final OffsetDateTime activeUntil) {
		this.id = id;
		this.user = user;
		this.refreshToken = refreshToken;
		this.activeUntil = activeUntil;
	}

	public SessionInfo info() {
		return new SessionInfo(id, user.getId(), refreshToken, activeUntil);
	}
}
