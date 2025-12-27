package com.cvesters.notula.organisation;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.organisation.bdo.OrganisationInfo;
import com.cvesters.notula.organisation.bdo.OrganisationUserInfo;

@Service
public class OrganisationService {

	private final OrganisationStorageGateway organisationStorage;
	private final OrganisationUserStorageGateway organisationUserStorage;

	public OrganisationService(
			final OrganisationStorageGateway organisationStorage,
			final OrganisationUserStorageGateway organisationUserStorage) {
		this.organisationStorage = organisationStorage;
		this.organisationUserStorage = organisationUserStorage;
	}

	public List<OrganisationInfo> getAll(final Principal principal) {
		Objects.requireNonNull(principal);

		final List<OrganisationUserInfo> organisationUsers = organisationUserStorage
				.findAllByUserId(principal.userId());
		final List<Long> organisationIds = organisationUsers.stream()
				.map(OrganisationUserInfo::getOrganisationId)
				.toList();

		return organisationStorage.findAllById(organisationIds);
	}

	public OrganisationInfo create(final Principal principal,
			final OrganisationInfo organisation) {
		Objects.requireNonNull(principal);
		Objects.requireNonNull(organisation);

		final OrganisationInfo createdOrganisation = organisationStorage
				.create(organisation);

		final var orgUserInfo = new OrganisationUserInfo(
				createdOrganisation.getId(), principal.userId());
		organisationUserStorage.create(orgUserInfo);

		return createdOrganisation;
	}
}
