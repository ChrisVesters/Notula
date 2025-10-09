package com.cvesters.notula.user;

import java.util.Objects;

import org.springframework.stereotype.Service;

import com.cvesters.notula.common.exception.DuplicateEntityException;
import com.cvesters.notula.user.bdo.UserInfo;
import com.cvesters.notula.user.bdo.UserLogin;

@Service
public class UserService {

	private final UserStorageGateway userStorageGateway;

	public UserService(final UserStorageGateway userStorageService) {
		this.userStorageGateway = userStorageService;
	}

	public UserInfo createUser(final UserLogin userLogin) {
		Objects.requireNonNull(userLogin);

		if (userStorageGateway.existsByEmail(userLogin.getEmail())) {
			throw new DuplicateEntityException();
		}

		return userStorageGateway.createUser(userLogin);
	}

}