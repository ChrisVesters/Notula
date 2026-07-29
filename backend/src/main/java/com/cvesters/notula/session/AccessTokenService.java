package com.cvesters.notula.session;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.cvesters.notula.config.JwtConfig;
import com.cvesters.notula.organisation.bdo.OrganisationUserRole;
import com.cvesters.notula.session.bdo.SessionInfo;

@Service
public class AccessTokenService {

	private static final Duration ACCESS_EXPIRATION = Duration.ofMinutes(30);

	private final JwtEncoder jwtEncoder;

	public AccessTokenService(final JwtEncoder jwtEncoder) {
		this.jwtEncoder = jwtEncoder;
	}

	public String create(final SessionInfo session,
			final OrganisationUserRole role) {
		Objects.requireNonNull(session);

		final var now = Instant.now();

		final var builder = JwtClaimsSet.builder()
				.subject(String.valueOf(session.getUserId()))
				.issuedAt(now)
				.expiresAt(now.plus(ACCESS_EXPIRATION));

		session.getOrganisationId()
				.ifPresent(organisationId -> builder.claim("organisation_id",
						organisationId));

		Optional.ofNullable(role).ifPresent(v -> builder.claim("role", role));

		final var claims = builder.build();
		final var header = JwsHeader.with(JwtConfig.MAC_ALGORITHM).build();

		return jwtEncoder.encode(JwtEncoderParameters.from(header, claims))
				.getTokenValue();
	}
}
