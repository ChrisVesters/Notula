package com.cvesters.notula.common.controller;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.cvesters.notula.common.domain.Principal;

public abstract class BaseController {

	protected Principal getPrincipal() {
		final Authentication authentication = SecurityContextHolder.getContext()
				.getAuthentication();
		if (authentication == null) {
			throw new IllegalStateException("User not authenticated");
		}

		if (authentication.getPrincipal() instanceof Jwt jwt) {
			final long userId = Long.parseLong(jwt.getSubject());

			final Long organisationId = Optional
					.ofNullable(jwt.getClaimAsString("organisation_id"))
					.map(Long::parseLong)
					.orElse(null);

			return new Principal(userId, organisationId);
		}

		throw new IllegalStateException(
				"Unsupported Authentication principal type: "
						+ authentication.getPrincipal().getClass().getName());
	}

	protected URI getLocation(final String path, final Object... uriVariables) {
		Objects.requireNonNull(path);
		Objects.requireNonNull(uriVariables);

		return ServletUriComponentsBuilder.fromCurrentRequest()
				.path(path)
				.buildAndExpand(uriVariables)
				.toUri();
	}
}
