package com.cvesters.notula.organisation;

import java.util.Objects;

import org.springframework.stereotype.Service;

import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.organisation.bdo.OrganisationInfo;
import com.cvesters.notula.organisation.bdo.OrganisationUserInfo;

@Service
public class OrganisationService {

	private final OrganisationUserService organisationUserService;
	private final OrganisationStorageGateway organisationStorage;

	public OrganisationService(
			final OrganisationUserService organisationUserService,
			final OrganisationStorageGateway organisationPersistenceGateway) {
		this.organisationUserService = organisationUserService;
		this.organisationStorage = organisationPersistenceGateway;
	}

	public OrganisationInfo create(final Principal principal,
			final OrganisationInfo organisation) {
		Objects.requireNonNull(principal);
		Objects.requireNonNull(organisation);

		final OrganisationInfo createdOrganisation = organisationStorage
				.create(organisation);

		final var orgUserInfo = new OrganisationUserInfo(
				createdOrganisation.getId(), principal.userId());
		organisationUserService.create(orgUserInfo);

		return createdOrganisation;
	}
}
