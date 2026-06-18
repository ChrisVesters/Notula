package com.cvesters.notula.organisation;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.cvesters.notula.organisation.bdo.OrganisationUserInfo;
import com.cvesters.notula.organisation.dao.OrganisationUserDao;

@Service
public class OrganisationUserStorageGateway {

	private final OrganisationUserRepository repository;

	public OrganisationUserStorageGateway(
			final OrganisationUserRepository repository) {
		this.repository = repository;
	}

	public List<OrganisationUserInfo> findAllByUserId(final long userId) {
		final var daos = repository.findAllByUserId(userId);
		return daos.stream().map(OrganisationUserDao::toBdo).toList();
	}

	public Optional<OrganisationUserInfo> findByUserIdAndOrganisationId(
			final long userId, final long organisationId) {
		return repository.findByUserIdAndOrganisationId(userId, organisationId)
				.map(OrganisationUserDao::toBdo);
	}

	public OrganisationUserInfo create(
			final OrganisationUserInfo organisationUser) {
		final var dao = new OrganisationUserDao(organisationUser);
		final var saved = repository.save(dao);
		return saved.toBdo();
	}
}
