package com.cvesters.notula.credential;

import java.util.Objects;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.cvesters.notula.credential.bdo.CredentialAction;
import com.cvesters.notula.credential.bdo.CredentialInfo;
import com.cvesters.notula.credential.dao.CredentialDao;

@Service
public class CredentialStorageGateway {

	private final PasswordEncoder passwordEncoder;

	private final CredentialRepository credentialRepository;

	public CredentialStorageGateway(
			final CredentialRepository credentialRepository,
			final PasswordEncoder passwordEncoder) {
		this.credentialRepository = credentialRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public CredentialInfo create(final CredentialAction.Create action) {
		Objects.requireNonNull(action);

		final long userId = action.getUserId();
		final String passwordHash = passwordEncoder
				.encode(action.getPassword().value());
		final var dao = new CredentialDao(userId, passwordHash);
		final CredentialDao created = credentialRepository.save(dao);

		return created.toBdo();
	}

	public boolean existsByUserId(final long userId) {
		return credentialRepository.existsByUserId(userId);
	}

	public Optional<CredentialInfo> findByLogin(
			final CredentialAction.Login login) {
		Objects.requireNonNull(login);

		final long userId = login.getUserId();
		final String password = login.getPassword().value();

		return credentialRepository.findByUserId(userId)
				.filter(u -> passwordEncoder.matches(password, u.getPassword()))
				.map(CredentialDao::toBdo);
	}
}
