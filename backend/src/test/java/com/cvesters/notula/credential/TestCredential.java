package com.cvesters.notula.credential;

import lombok.Getter;

import com.cvesters.notula.common.domain.Password;
import com.cvesters.notula.credential.bdo.CredentialAction;
import com.cvesters.notula.credential.bdo.CredentialInfo;
import com.cvesters.notula.user.TestUser;

@Getter
public enum TestCredential {
	EDUARDO_CHRISTIANSEN(1L, TestUser.EDUARDO_CHRISTIANSEN, "bbkpHh_hKk6KMwv"),
	KRISTINA_THIEL(2L, TestUser.KRISTINA_THIEL, "wLITAlWOYY5J8ms"),
	DAPHNEE_LESCH(3L, TestUser.DAPHNEE_LESCH, "VIz3jmembRtsuoo"),
	ALISON_DACH(4L, TestUser.ALISON_DACH, "YIHS3bbkpHh_hKk");

	private final long id;
	private final long userId;
	private final Password password;

	TestCredential(final long id, final TestUser user, final String password) {
		this.id = id;
		this.userId = user.getId();
		this.password = new Password(password);
	}

	public CredentialInfo info() {
		return new CredentialInfo(id, userId);
	}

	public CredentialAction.Create create() {
		return new CredentialAction.Create(userId, password);
	}

	public CredentialAction.Login login() {
		return new CredentialAction.Login(userId, password);
	}
}
