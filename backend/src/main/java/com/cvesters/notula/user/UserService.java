package com.cvesters.notula.user;

import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.cvesters.notula.common.exception.DuplicateEntityException;
import com.cvesters.notula.user.bdo.UserInfo;
import com.cvesters.notula.user.bdo.UserLogin;

@Service
public class UserService {

	private final UserStorageGateway userStorage;

	public UserService(final UserStorageGateway userStorageService) {
		this.userStorage = userStorageService;
	}

	public UserInfo createUser(final UserLogin userLogin) {
		Objects.requireNonNull(userLogin);

		if (userStorage.existsByEmail(userLogin.getEmail())) {
			throw new DuplicateEntityException();
		}

		return userStorage.create(userLogin);
	}

	public Optional<UserInfo> findByLogin(final UserLogin userLogin) {
		Objects.requireNonNull(userLogin);

		return userStorage.findByLogin(userLogin);
	}

}