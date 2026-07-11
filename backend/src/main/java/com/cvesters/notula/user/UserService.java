package com.cvesters.notula.user;

import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.cvesters.notula.common.domain.Email;
import com.cvesters.notula.common.exception.DuplicateEntityException;
import com.cvesters.notula.user.bdo.UserInfo;

@Service
public class UserService {

	private final UserStorageGateway userStorage;

	public UserService(final UserStorageGateway userStorageService) {
		this.userStorage = userStorageService;
	}

	public UserInfo createUser(final UserInfo userInfo) {
		Objects.requireNonNull(userInfo);

		if (userStorage.existsByEmail(userInfo.getEmail())) {
			throw new DuplicateEntityException();
		}

		return userStorage.create(userInfo);
	}

	public Optional<UserInfo> findByEmail(final Email email) {
		Objects.requireNonNull(email);

		return userStorage.findByEmail(email);
	}

}