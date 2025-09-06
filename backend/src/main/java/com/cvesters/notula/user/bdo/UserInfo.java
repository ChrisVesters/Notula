package com.cvesters.notula.user.bdo;

import lombok.Getter;

@Getter
public class UserInfo {

	private Long id;
	private String email;

	public UserInfo(Long id, String email) {
		this.id = id;
		this.email = email;
	}
}
