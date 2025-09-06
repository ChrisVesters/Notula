package com.cvesters.notula.organisation;

import java.util.Objects;

import org.springframework.stereotype.Service;

import com.cvesters.notula.organisation.bdo.Organisation;
import com.cvesters.notula.organisation.dao.OrganisationDao;

@Service
public class OrganisationStorageGateway {

	private final OrganisationRepository repository;

	public OrganisationStorageGateway(final OrganisationRepository repository) {
		this.repository = repository;
	}

	public Organisation create(final Organisation organisation) {
		Objects.requireNonNull(organisation);

		final var dao = OrganisationDao.fromBdo(organisation);
		final var saved = repository.save(dao);
		return saved.toBdo();
	}

}
