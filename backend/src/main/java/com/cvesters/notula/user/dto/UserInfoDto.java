package com.cvesters.notula.user.dto;

import com.cvesters.notula.user.bdo.UserInfo;

public record UserInfoDto(Long id, String email) {

	public UserInfoDto(final UserInfo userInfo) {
		this(userInfo.getId(), userInfo.getEmail().value());
	}
}
