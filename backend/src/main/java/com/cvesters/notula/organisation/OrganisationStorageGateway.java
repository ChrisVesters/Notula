package com.cvesters.notula.organisation;

import java.util.Objects;

import org.springframework.stereotype.Service;

import com.cvesters.notula.organisation.bdo.OrganisationInfo;
import com.cvesters.notula.organisation.dao.OrganisationDao;

@Service
public class OrganisationStorageGateway {

	private final OrganisationRepository repository;

	public OrganisationStorageGateway(final OrganisationRepository repository) {
		this.repository = repository;
	}

	public OrganisationInfo create(final OrganisationInfo organisation) {
		Objects.requireNonNull(organisation);

		final var dao = new OrganisationDao(organisation);
		final var saved = repository.save(dao);
		return saved.toBdo();
	}

}
