package com.cvesters.notula.common.domain;

import java.util.Objects;

import org.apache.commons.lang3.Validate;

public record Email(String value) {

	public Email(final String value) {
		Objects.requireNonNull(value);
		Validate.notBlank(value);
		// Validate.matchesPattern(email, password);
		
		// TODO: LOWERCASE!!!
		this.value = value;
	}
}
