package com.cvesters.notula.organisation;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.organisation.bdo.OrganisationUserInfo;

@Service
public class OrganisationUserService {

	private final OrganisationUserStorageGateway organisationUserStorage;

	public OrganisationUserService(
			final OrganisationUserStorageGateway organisationUserStorage) {
		this.organisationUserStorage = organisationUserStorage;
	}

	public List<OrganisationUserInfo> getAll(final Principal principal) {
		Objects.requireNonNull(principal);

		return organisationUserStorage.findAllByUserId(principal.userId());
	}
}
