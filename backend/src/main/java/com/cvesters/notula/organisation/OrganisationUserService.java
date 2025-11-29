package com.cvesters.notula.organisation;

import java.util.Objects;

import org.springframework.stereotype.Service;

import com.cvesters.notula.organisation.bdo.OrganisationUserInfo;

@Service
public class OrganisationUserService {

	private final OrganisationUserStorageGateway organisationUserStorage;

	public OrganisationUserService(
			final OrganisationUserStorageGateway organisationUserStorageGateway) {
		this.organisationUserStorage = organisationUserStorageGateway;
	}

	public OrganisationUserInfo create(
			final OrganisationUserInfo organisationUser) {
		Objects.requireNonNull(organisationUser);

		return organisationUserStorage.create(organisationUser);
	}
}
