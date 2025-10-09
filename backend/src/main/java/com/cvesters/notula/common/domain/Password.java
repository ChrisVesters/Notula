package com.cvesters.notula.common.domain;

import java.util.Objects;

import org.apache.commons.lang3.Validate;

public record Password(String value) {

	private static final int MIN_LENGTH = 8;

	public Password {
		Objects.requireNonNull(value);
		Validate.isTrue(value.length() >= MIN_LENGTH);
	}
}
