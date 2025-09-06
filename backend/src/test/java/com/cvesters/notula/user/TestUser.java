package com.cvesters.notula.user;

// TODO: enum or record class?
public enum TestUser {
	EDUARDO_CHRISTIANSEN(1L, "eduardo.christiansen@sporer.com", "bbkpHh_hKk6KMwv"),
	JUDY_HARBER(2L, "judy.harber@sporer.com", "wLITAlWOYY5J8ms");

	private final long id;
	private final String email;
	private final String password;

	private TestUser(final long id, final String email, final String password) {
		this.id = id;
		this.email = email;
		this.password = password;
	}

}
