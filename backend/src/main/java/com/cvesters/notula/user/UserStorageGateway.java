package com.cvesters.notula.user;

import java.util.Objects;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.cvesters.notula.common.domain.Email;
import com.cvesters.notula.user.bdo.UserInfo;
import com.cvesters.notula.user.bdo.UserLogin;
import com.cvesters.notula.user.dao.UserDao;

@Service
public class UserStorageGateway {

	private final UserRepository userRepository;

	private final PasswordEncoder passwordEncoder;

	public UserStorageGateway(final UserRepository userRepository,
			final PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public boolean existsByEmail(final Email email) {
		Objects.requireNonNull(email);

		return userRepository.existsByEmail(email.value());
	}

	public Optional<UserInfo> findByLogin(final UserLogin login) {
		Objects.requireNonNull(login);

		final String email = login.getEmail().value();
		final String password = login.getPassword().value();

		return userRepository.findByEmail(email)
				.filter(u -> passwordEncoder.matches(password, u.getPassword()))
				.map(UserDao::toBdo);
	}

	public UserInfo createUser(final UserLogin login) {
		Objects.requireNonNull(login);

		final String passwordHash = passwordEncoder
				.encode(login.getPassword().value());
		final var dao = new UserDao(login.getEmail().value(), passwordHash);
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
