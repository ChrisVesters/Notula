package com.cvesters.notula.organisation;

import java.util.Objects;

import org.springframework.stereotype.Service;

import com.cvesters.notula.organisation.bdo.Organisation;

@Service
public class OrganisationService {

	private final OrganisationStorageGateway organisationStorage;

	public OrganisationService(final OrganisationStorageGateway organisationPersistenceGateway) {
		this.organisationStorage = organisationPersistenceGateway;
	}

	public Organisation create(final Organisation organisation) {
		Objects.requireNonNull(organisation);

		final Organisation created = organisationStorage.create(organisation);
		
		return created;
	}

}
