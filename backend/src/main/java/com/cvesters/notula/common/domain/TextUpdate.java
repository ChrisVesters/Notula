package com.cvesters.notula.common.domain;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.apache.commons.lang3.Validate;

import lombok.AccessLevel;
import lombok.Getter;

@Getter
public class TextUpdate<T> {

	@Getter(AccessLevel.NONE)
	private final Function<T, String> getter;

	@Getter(AccessLevel.NONE)
	private final BiConsumer<T, String> setter;

	private final int position;
	private final int length;
	private final String value;

	protected TextUpdate(final Function<T, String> getter,
			final BiConsumer<T, String> setter, final int position,
			final int length, final String value) {
		Objects.requireNonNull(getter);
		Objects.requireNonNull(setter);
		Objects.requireNonNull(value);
		Validate.isTrue(position >= 0);
		Validate.isTrue(length >= 0);

		this.getter = getter;
		this.setter = setter;

		this.position = position;
		this.length = length;
		this.value = value;
	}

	public final void apply(final T object) {
		Objects.requireNonNull(object);

		final String current = getter.apply(object);
		final String updated = execute(current);
		setter.accept(object, updated);
	}

	protected String execute(final String input) {
		Objects.requireNonNull(input);
		Validate.isTrue(input.length() >= position + length);

		final String prefix = input.substring(0, position);
		final String suffix = input.substring(position + length);

		return prefix + value + suffix;
	}
}
