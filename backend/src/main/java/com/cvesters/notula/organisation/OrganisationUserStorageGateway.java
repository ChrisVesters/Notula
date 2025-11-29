package com.cvesters.notula.organisation;

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

	public OrganisationUserInfo create(final OrganisationUserInfo organisationUser) {
		final var dao = new OrganisationUserDao(organisationUser);
		final var saved = repository.save(dao);
		return saved.toBdo();
	}
}
