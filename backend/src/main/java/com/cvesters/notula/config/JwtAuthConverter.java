package com.cvesters.notula.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import com.cvesters.notula.common.domain.Principal;

@Component
public class JwtAuthConverter
		implements Converter<Jwt, AbstractAuthenticationToken> {

	// TODO: Some enum?!
	public static final String ORGANISATION_CLAIM = "CLAIM_ORGANISATION";

	@Override
	public AbstractAuthenticationToken convert(final Jwt jwt) {
		final long userId = Long.parseLong(jwt.getSubject());

		// TODO: no magic strings
		final Long organisationId = Optional
				.ofNullable(jwt.getClaimAsString("organisation_id"))
				.map(Long::parseLong)
				.orElse(null);

		final var authorities = new ArrayList<GrantedAuthority>();
		if (jwt.hasClaim("organisation_id")) {
			authorities.add(new SimpleGrantedAuthority(ORGANISATION_CLAIM));
		}

		final var principal = new Principal(userId, organisationId);
		return new AuthToken(principal, authorities);
	}

	public static class AuthToken extends AbstractAuthenticationToken {

		public Principal principal;

		public AuthToken(final Principal principal,
				final Collection<GrantedAuthority> authorities) {
			super(authorities);

			this.principal = principal;
			this.setAuthenticated(true);
		}

		@Override
		public Object getCredentials() {
			return null;
		}

		@Override
		public Principal getPrincipal() {
			return principal;
		}
	}
}
