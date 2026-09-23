package com.cvesters.notula.common.domain;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.apache.commons.lang3.Validate;

public record Rank(String value) implements Comparable<Rank> {

	private static final String DIGITS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

	private static final char FIRST = DIGITS.charAt(0);
	private static final char LAST = DIGITS.charAt(DIGITS.length() - 1);
	private static final char MIDDLE = DIGITS.charAt(DIGITS.length() / 2);

	public Rank {
		Validate.notBlank(value);
		Validate.isTrue(value.chars().allMatch(c -> DIGITS.indexOf(c) >= 0));
		Validate.isTrue(value.charAt(value.length() - 1) != FIRST);
	}

	public Rank() {
		this(String.valueOf(MIDDLE));
	}

	public static <O> Rank after(final O after, final List<O> elements,
			final Function<O, Rank> rank) {
		Objects.requireNonNull(elements);
		Objects.requireNonNull(rank);

		if (after == null) {
			return elements.stream()
					.findFirst()
					.map(rank::apply)
					.map(Rank::before)
					.orElseGet(Rank::new);
		}

		final int afterIndex = elements.indexOf(after);
		final int beforeIndex = afterIndex + 1;
		if (beforeIndex >= elements.size()) {
			final Rank afterRank = rank.apply(after);
			return Rank.after(afterRank);
		}

		final O before = elements.get(afterIndex + 1);

		final Rank afterRank = rank.apply(after);
		final Rank beforeRank = rank.apply(before);

		return Rank.between(afterRank, beforeRank);
	}

	private static Rank between(final Rank lower, final Rank upper) {
		Objects.requireNonNull(lower);
		Objects.requireNonNull(upper);
		Validate.isTrue(lower.compareTo(upper) < 0);

		final String lowerValue = lower.value();
		final String upperValue = upper.value();

		final var between = new StringBuilder();
		boolean tied = true;

		for (int index = 0;; index++) {
			final int low = index < lowerValue.length()
					? index(lowerValue.charAt(index)) : 0;
			final int high = tied ? index(upperValue.charAt(index))
					: DIGITS.length();

			final int middle = (low + high) / 2;
			if (middle > low) {
				between.append(DIGITS.charAt(middle));
				return new Rank(between.toString());
			}

			between.append(DIGITS.charAt(low));
			tied &= (low == high);
		}
	}

	private static Rank before(final Rank upper) {
		Objects.requireNonNull(upper);

		final String value = upper.value();
		final String prefix = value.substring(0, value.length() - 1);

		final int lastCharIndex = index(value.charAt(value.length() - 1));
		final char precedingChar = DIGITS.charAt(lastCharIndex - 1);

		if (precedingChar == FIRST) {
			return new Rank(prefix + precedingChar + MIDDLE);
		} else {
			return new Rank(prefix + precedingChar);
		}
	}

	private static Rank after(final Rank lower) {
		Objects.requireNonNull(lower);

		final String value = lower.value();
		final char lastChar = value.charAt(value.length() - 1);

		if (lastChar == LAST) {
			return new Rank(value + MIDDLE);
		} else {
			final String prefix = value.substring(0, value.length() - 1);
			final char followingChar = DIGITS.charAt(index(lastChar) + 1);
			return new Rank(prefix + followingChar);
		}
	}

	private static int index(final char digit) {
		return DIGITS.indexOf(digit);
	}

	@Override
	public int compareTo(final Rank other) {
		return value().compareTo(other.value());
	}
}
