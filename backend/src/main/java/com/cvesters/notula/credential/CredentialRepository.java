package com.cvesters.notula.credential;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import com.cvesters.notula.credential.dao.CredentialDao;

public interface CredentialRepository extends Repository<CredentialDao, Long> {

	boolean existsByUserId(final long userId);

	Optional<CredentialDao> findByUserId(final long userId);

	CredentialDao save(final CredentialDao credential);
}