package com.cvesters.notula.organisation.bdo;

import java.util.Objects;

import org.apache.commons.lang3.Validate;

import lombok.Getter;

@Getter
public class Organisation {

	private final Long id;
	private final String name;

	public Organisation(final String name) {
		this(null, name);
	}

	public Organisation(final Long id, final String name) {
		Objects.requireNonNull(name);
		Validate.notBlank(name);

		this.id = id;
		this.name = name;
	}
}
