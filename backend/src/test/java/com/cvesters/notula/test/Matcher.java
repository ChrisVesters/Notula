package com.cvesters.notula.test;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.Condition;

public abstract class Matcher<T> {

	protected final T expected;
	private final Class<T> expectedClass;

	protected Matcher(final T expected, final Class<T> expectedClass) {
		this.expected = expected;
		this.expectedClass = expectedClass;
	}

	public final boolean matches(final T actual) {
		return this.equal().matches(actual);
	}

	public final Condition<Object> equal() {
		return new Condition<>(actual -> {
			assertThat(actual).isExactlyInstanceOf(expectedClass);
			assertEquals(expectedClass.cast(actual));
			return true;
		}
		, "equal");
	}

	protected abstract void assertEquals(final T actual);
}
