package com.cvesters.notula.session;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.common.exception.MissingEntityException;
import com.cvesters.notula.credential.CredentialService;
import com.cvesters.notula.credential.bdo.CredentialAction;
import com.cvesters.notula.organisation.OrganisationUserService;
import com.cvesters.notula.organisation.bdo.OrganisationUserInfo;
import com.cvesters.notula.organisation.bdo.OrganisationUserRole;
import com.cvesters.notula.session.bdo.SessionAction;
import com.cvesters.notula.session.bdo.SessionInfo;
import com.cvesters.notula.session.bdo.SessionTokens;
import com.cvesters.notula.session.bdo.SessionUpdate;
import com.cvesters.notula.user.UserService;
import com.cvesters.notula.user.bdo.UserInfo;

@Service
public class SessionService {

	private static final SecureRandom RANDOM = new SecureRandom();

	private final UserService userService;
	private final CredentialService credentialService;
	private final OrganisationUserService organisationUserService;
	private final AccessTokenService accessTokenService;

	private final SessionStorageGateway sessionStorage;

	public SessionService(final UserService userService,
			final CredentialService credentialService,
			final OrganisationUserService organisationUserService,
			final AccessTokenService accessTokenService,
			final SessionStorageGateway sessionStorageGateway) {
		this.userService = userService;
		this.credentialService = credentialService;
		this.organisationUserService = organisationUserService;
		this.accessTokenService = accessTokenService;
		this.sessionStorage = sessionStorageGateway;
	}

	public SessionTokens create(final SessionAction.Create request) {
		Objects.requireNonNull(request);

		final UserInfo user = userService.findByEmail(request.getEmail())
				.filter(u -> credentialService
						.existsLogin(new CredentialAction.Login(u.getId(),
								request.getPassword())))
				.orElseThrow(MissingEntityException::new);

		final Optional<OrganisationUserInfo> defaultOrganisation = getDefaultOrganisation(
				user);

		final Long defaultOrganisationId = defaultOrganisation
				.map(OrganisationUserInfo::getOrganisationId)
				.orElse(null);
		final var action = new SessionInfo(user.getId(), defaultOrganisationId);
		final String refreshToken = generateRefreshToken();

		final SessionInfo createdSession = sessionStorage.create(action,
				refreshToken);

		final OrganisationUserRole role = defaultOrganisation
				.map(OrganisationUserInfo::getRole)
				.orElse(null);
		final String accessToken = accessTokenService.create(createdSession,
				role);

		return new SessionTokens(createdSession, accessToken, refreshToken);
	}

	public SessionTokens update(final Principal principal, final long sessionId,
			final SessionUpdate update) {
		Objects.requireNonNull(principal);
		Objects.requireNonNull(update);

		final OrganisationUserInfo orgUser = organisationUserService
				.getAllForUser(principal)
				.stream()
				.filter(ou -> ou.getOrganisationId() == update.organisationId())
				.findFirst()
				.orElseThrow(MissingEntityException::new);

		final SessionInfo bdo = sessionStorage.findById(sessionId)
				.filter(session -> session.getUserId() == principal.userId())
				.filter(SessionInfo::isActive)
				.orElseThrow(MissingEntityException::new);

		bdo.update(update);

		final SessionInfo session = sessionStorage.update(bdo);
		final String accessToken = accessTokenService.create(session,
				orgUser.getRole());

		return new SessionTokens(session, accessToken);
	}

	public SessionTokens refresh(final long sessionId,
			final String refreshToken) {
		Objects.requireNonNull(refreshToken);

		final SessionInfo bdo = sessionStorage
				.findByIdAndRefreshToken(sessionId, refreshToken)
				.filter(SessionInfo::isActive)
				.orElseThrow(MissingEntityException::new);

		final OrganisationUserRole role = bdo.getOrganisationId()
				.map(orgId -> new Principal(bdo.getUserId(), orgId))
				.map(principal -> organisationUserService
						.findByUserIdAndOrganisationId(principal)
						.orElseThrow(MissingEntityException::new))
				.map(OrganisationUserInfo::getRole)
				.orElse(null);

		bdo.refresh();
		final String newToken = generateRefreshToken();

		final SessionInfo session = sessionStorage.update(bdo, newToken);
		final String accessToken = accessTokenService.create(session, role);

		return new SessionTokens(session, accessToken, newToken);
	}

	public void delete(final Principal principal, long sessionId) {
		Objects.requireNonNull(principal);

		final SessionInfo bdo = sessionStorage.findById(sessionId)
				.filter(session -> session.getUserId() == principal.userId())
				.filter(SessionInfo::isActive)
				.orElseThrow(MissingEntityException::new);

		bdo.invactivate();
		sessionStorage.update(bdo);
	}

	private static String generateRefreshToken() {
		final var bytes = new byte[64];
		RANDOM.nextBytes(bytes);

		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	private Optional<OrganisationUserInfo> getDefaultOrganisation(
			final UserInfo user) {
		final var principal = new Principal(user.getId());
		final List<OrganisationUserInfo> organisations = organisationUserService
				.getAllForUser(principal);
		if (organisations.size() == 1) {
			return Optional.of(organisations.getFirst());
		} else {
			return Optional.empty();
		}
	}
}
