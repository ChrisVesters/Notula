package com.cvesters.notula.common.domain;

import java.util.Locale;
import java.util.Objects;

import org.apache.commons.lang3.Validate;

public record Email(String value) {

	public Email(final String value) {
		Objects.requireNonNull(value);
		Validate.matchesPattern(value, "[^@]+@[^@]+");
		
		this.value = value.toLowerCase(Locale.ROOT);
	}
}
