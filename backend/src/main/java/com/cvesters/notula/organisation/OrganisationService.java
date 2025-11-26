package com.cvesters.notula.organisation;

import java.util.Objects;

import org.springframework.stereotype.Service;

import com.cvesters.notula.organisation.bdo.OrganisationInfo;

@Service
public class OrganisationService {

	private final OrganisationStorageGateway organisationStorage;

	public OrganisationService(final OrganisationStorageGateway organisationPersistenceGateway) {
		this.organisationStorage = organisationPersistenceGateway;
	}

	public OrganisationInfo create(final OrganisationInfo organisation) {
		Objects.requireNonNull(organisation);

		return organisationStorage.create(organisation);
	}
}
