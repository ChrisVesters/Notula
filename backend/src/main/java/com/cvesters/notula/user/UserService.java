package com.cvesters.notula.user;

import java.util.Objects;

import org.springframework.stereotype.Service;

import com.cvesters.notula.user.bdo.UserInfo;
import com.cvesters.notula.user.bdo.UserLogin;

@Service
public class UserService {

	private final UserStorageService userStorageService;

	public UserService(final UserStorageService userStorageService) {
		this.userStorageService = userStorageService;
	}

	public UserInfo createUser(final UserLogin userLogin) {
		Objects.requireNonNull(userLogin);

		return userStorageService.createUser(userLogin);
	}

}