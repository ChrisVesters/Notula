package com.cvesters.notula.organisation;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.common.exception.DuplicateEntityException;
import com.cvesters.notula.common.exception.MissingEntityException;
import com.cvesters.notula.organisation.bdo.OrganisationUserAction;
import com.cvesters.notula.organisation.bdo.OrganisationUserInfo;
import com.cvesters.notula.organisation.bdo.OrganisationUserView;
import com.cvesters.notula.user.UserService;
import com.cvesters.notula.user.bdo.UserInfo;

@Service
public class OrganisationUserService {

	private final UserService userService;

	private final OrganisationUserStorageGateway organisationUserStorage;

	public OrganisationUserService(final UserService userService,
			final OrganisationUserStorageGateway organisationUserStorage) {
		this.userService = userService;
		this.organisationUserStorage = organisationUserStorage;
	}

	public List<OrganisationUserInfo> getAllForUser(final Principal principal) {
		Objects.requireNonNull(principal);

		return organisationUserStorage.findAllByUserId(principal.userId());
	}

	public List<OrganisationUserView> getAllForOrganisation(
			final Principal principal) {
		Objects.requireNonNull(principal);

		return organisationUserStorage
				.findAllByOrganisationId(principal.organisationId());
	}

	public OrganisationUserInfo create(final Principal principal,
			final OrganisationUserAction.Create action) {
		Objects.requireNonNull(principal);
		Objects.requireNonNull(action);

		final UserInfo userInfo = userService.findByEmail(action.getEmail())
				.orElseThrow(MissingEntityException::new);

		final Optional<OrganisationUserInfo> existing = organisationUserStorage
				.findByUserIdAndOrganisationId(userInfo.getId(),
						principal.organisationId());
		if (existing.isPresent()) {
			throw new DuplicateEntityException();
		}

		final var info = new OrganisationUserInfo(principal.organisationId(),
				userInfo.getId());
		return organisationUserStorage.create(info);
	}
}
