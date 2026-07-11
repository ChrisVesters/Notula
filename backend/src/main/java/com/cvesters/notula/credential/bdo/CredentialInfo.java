package com.cvesters.notula.credential.bdo;

import lombok.Getter;

@Getter
public class CredentialInfo {

	private final long id;
	private final long userId;

	public CredentialInfo(final long id, final long userId) {
		this.id = id;
		this.userId = userId;
	}
}
