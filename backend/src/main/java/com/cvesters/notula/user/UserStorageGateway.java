package com.cvesters.notula.user;

import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.cvesters.notula.common.domain.Email;
import com.cvesters.notula.user.bdo.UserInfo;
import com.cvesters.notula.user.dao.UserDao;

@Service
public class UserStorageGateway {

	private final UserRepository userRepository;

	public UserStorageGateway(final UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public Optional<UserInfo> findByEmail(final Email email) {
		Objects.requireNonNull(email);

		return userRepository.findByEmail(email.value()).map(UserDao::toBdo);
	}

	public boolean existsByEmail(final Email email) {
		Objects.requireNonNull(email);

		return userRepository.existsByEmail(email.value());
	}

	public UserInfo create(final UserInfo info) {
		Objects.requireNonNull(info);

		final var dao = new UserDao(info.getEmail().value());
		final UserDao created = userRepository.save(dao);
		return created.toBdo();
	}

	// public UserInfo updateUser(final UserInfo info) {
	// final UserDao dao = userRepository.findById(info.getId());

	// // TODO: allow update of email?
	// dao.setEmail(info.email());

	// final UserDao updated = userRepository.save(dao);
	// return updated.toBdo();
	// }
}
