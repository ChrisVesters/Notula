package com.cvesters.notula.user;

import java.util.Objects;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.cvesters.notula.user.bdo.UserInfo;
import com.cvesters.notula.user.bdo.UserLogin;
import com.cvesters.notula.user.dao.UserDao;

@Service
public class UserStorageService {

	private final UserRepository userRepository;

	private final PasswordEncoder passwordEncoder;

	public UserStorageService(final UserRepository userRepository,
			final PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public UserInfo createUser(final UserLogin login) {
		Objects.requireNonNull(login);

		final String passwordHash = passwordEncoder.encode(login.getPassword());
		final var dao = new UserDao(login.getEmail(), passwordHash);
		final UserDao created = userRepository.save(dao);
		return created.toBdo();
	}
}
