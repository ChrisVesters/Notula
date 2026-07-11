package com.cvesters.notula.credential;

import java.util.Objects;

import org.springframework.stereotype.Service;

import com.cvesters.notula.common.exception.DuplicateEntityException;
import com.cvesters.notula.credential.bdo.CredentialAction;
import com.cvesters.notula.credential.bdo.CredentialInfo;

@Service
public class CredentialService {

	private final CredentialStorageGateway credentialStorageGateway;

	public CredentialService(
			final CredentialStorageGateway credentialStorageGateway) {
		this.credentialStorageGateway = credentialStorageGateway;
	}

	public CredentialInfo create(final CredentialAction.Create action) {
		Objects.requireNonNull(action);

		if (credentialStorageGateway.existsByUserId(action.getUserId())) {
			throw new DuplicateEntityException();
		}

		return credentialStorageGateway.create(action);
	}

	public boolean existsLogin(final CredentialAction.Login login) {
		Objects.requireNonNull(login);

		return credentialStorageGateway.findByLogin(login).isPresent();
	}
}
