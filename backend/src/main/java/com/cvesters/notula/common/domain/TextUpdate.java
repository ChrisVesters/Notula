package com.cvesters.notula.common.domain;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

import lombok.AccessLevel;
import lombok.Getter;

@Getter
public class TextUpdate<T> {

	@Getter(AccessLevel.NONE)
	private final Function<T, String> getter;

	@Getter(AccessLevel.NONE)
	private final BiConsumer<T, String> setter;

	private final Splice edit;

	protected TextUpdate(final Function<T, String> getter,
			final BiConsumer<T, String> setter, final Splice edit) {
		Objects.requireNonNull(getter);
		Objects.requireNonNull(setter);
		Objects.requireNonNull(edit);

		this.getter = getter;
		this.setter = setter;
		this.edit = edit;
	}

	public final void apply(final T object) {
		Objects.requireNonNull(object);

		final String current = getter.apply(object);
		setter.accept(object, edit.applyTo(current));
	}
}
