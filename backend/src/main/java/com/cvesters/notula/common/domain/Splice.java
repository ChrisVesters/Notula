package com.cvesters.notula.common.domain;

import java.util.Objects;

import org.apache.commons.lang3.Validate;

public record Splice(int position, int length, String value) {

	public Splice {
		Objects.requireNonNull(value);
		Validate.isTrue(position >= 0);
		Validate.isTrue(length >= 0);
	}

	public int end() {
		return position + length;
	}

	public String applyTo(final String text) {
		Objects.requireNonNull(text);
		Validate.isTrue(text.length() >= end());

		return text.substring(0, position) + value + text.substring(end());
	}

	public Splice rebasedOnto(final Splice prior) {
		Objects.requireNonNull(prior);

		final int start = prior.startAfter(position);
		final int stop = Math.max(start, prior.stopAfter(end()));

		// Whatever the prior edit inserted inside the range this one replaces
		// was typed by someone, so it is put back rather than deleted with it.
		final boolean covers = position < prior.position
				&& end() > prior.end();
		final String inserted = covers ? value + prior.value : value;

		return new Splice(start, stop - start, inserted);
	}

	private int startAfter(final int index) {
		if (index < position) {
			return index;
		}

		if (index <= end()) {
			return position + value.length();
		}

		return index - length + value.length();
	}

	private int stopAfter(final int index) {
		if (index <= position) {
			return index;
		}

		if (index <= end()) {
			return position;
		}

		return index - length + value.length();
	}
}
