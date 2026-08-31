package com.cvesters.notula.common.domain;

import java.util.Objects;
import java.util.UUID;

public record Origin(Principal principal, UUID clientId) {

	public Origin {
		Objects.requireNonNull(principal);
	}

	public Origin(final Principal principal) {
		this(principal, null);
	}

	public long userId() {
		return principal.userId();
	}
}
