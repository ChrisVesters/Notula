package com.cvesters.notula.common.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SpliceTest {

	private static final String TEXT = "abcd";

	@Nested
	class Constructor {

		@Test
		void success() {
			final var splice = new Splice(1, 2, "x");

			assertThat(splice.position()).isEqualTo(1);
			assertThat(splice.length()).isEqualTo(2);
			assertThat(splice.value()).isEqualTo("x");
			assertThat(splice.end()).isEqualTo(3);
		}

		@Test
		void start() {
			final var splice = new Splice(0, 2, "x");

			assertThat(splice.position()).isEqualTo(0);
		}

		@Test
		void insert() {
			final var splice = new Splice(1, 0, "x");

			assertThat(splice.length()).isEqualTo(0);
		}

		@Test
		void positionNegative() {
			assertThatThrownBy(() -> new Splice(-1, 0, "x"))
					.isInstanceOf(IllegalArgumentException.class);
		}

		@Test
		void lengthNegative() {
			assertThatThrownBy(() -> new Splice(0, -1, "x"))
					.isInstanceOf(IllegalArgumentException.class);
		}

		@Test
		void valueNull() {
			assertThatThrownBy(() -> new Splice(0, 0, null))
					.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class ApplyTo {

		@ParameterizedTest
		@CsvSource({ "0,0,x,xabcd", "2,0,x,abxcd", "4,0,x,abcdx",
				"1,2,'',ad", "1,2,x,axd", "0,4,x,x" })
		void success(final int position, final int length, final String value,
				final String expected) {
			final var splice = new Splice(position, length, value);

			assertThat(splice.applyTo(TEXT)).isEqualTo(expected);
		}

		@ParameterizedTest
		@CsvSource({ "5,0", "4,1", "0,5" })
		void outOfBounds(final int position, final int length) {
			final var splice = new Splice(position, length, "x");

			assertThatThrownBy(() -> splice.applyTo(TEXT))
					.isInstanceOf(IllegalArgumentException.class);
		}

		@Test
		void textNull() {
			final var splice = new Splice(0, 0, "x");

			assertThatThrownBy(() -> splice.applyTo(null))
					.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class RebasedOnto {

		@ParameterizedTest
		@CsvSource({
				"0,1,x,2,1,p,0,1,x",
				"0,1,x,1,1,p,0,1,x",
				"3,1,x,0,1,p,3,1,x",
				"3,1,x,0,2,pq,3,1,x",
				"3,1,x,0,0,p,4,1,x",
				"2,0,x,2,0,p,3,0,x",
				"2,0,x,1,2,p,2,0,x",
				"2,0,x,1,1,p,2,0,x",
				"1,1,x,1,1,p,2,0,x",
				"0,2,x,1,2,p,0,1,x",
				"1,2,x,0,2,p,1,1,x",
				"0,4,x,1,2,p,0,3,xp",
				"0,4,x,2,0,p,0,5,xp",
				"1,2,'',0,4,p,1,0,''",
				"2,2,x,2,0,p,3,2,x" })
		void success(final int position, final int length, final String value,
				final int priorPosition, final int priorLength,
				final String priorValue, final int expectedPosition,
				final int expectedLength, final String expectedValue) {
			final var splice = new Splice(position, length, value);
			final var prior = new Splice(priorPosition, priorLength,
					priorValue);

			final Splice rebased = splice.rebasedOnto(prior);

			assertThat(rebased).isEqualTo(new Splice(expectedPosition,
					expectedLength, expectedValue));
		}

		@Test
		void exhaustive() {
			final var splices = new ArrayList<Splice>();
			final var priors = new ArrayList<Splice>();
			for (int position = 0; position <= TEXT.length(); position++) {
				for (int end = position; end <= TEXT.length(); end++) {
					for (final String value : List.of("", "x", "xy")) {
						splices.add(new Splice(position, end - position,
								value));
					}
					for (final String value : List.of("", "p", "pq")) {
						priors.add(new Splice(position, end - position,
								value));
					}
				}
			}

			for (final Splice splice : splices) {
				for (final Splice prior : priors) {
					// The text both edits mean together, built without
					// transforming anything, so that it checks the transform
					// rather than restating it.
					final var merged = new StringBuilder();
					for (int index = 0; index <= TEXT.length(); index++) {
						if (index == prior.position()) {
							merged.append(prior.value());
						}
						if (index == splice.position()) {
							merged.append(splice.value());
						}

						final boolean removedBySplice = index >= splice
								.position() && index < splice.end();
						final boolean removedByPrior = index >= prior
								.position() && index < prior.end();
						if (index < TEXT.length() && !removedBySplice
								&& !removedByPrior) {
							merged.append(TEXT.charAt(index));
						}
					}

					final Splice rebased = splice.rebasedOnto(prior);

					assertThat(rebased.applyTo(prior.applyTo(TEXT)))
							.as("%s onto %s", splice, prior)
							.isEqualTo(merged.toString());
				}
			}
		}

		@Test
		void priorNull() {
			final var splice = new Splice(0, 0, "x");

			assertThatThrownBy(() -> splice.rebasedOnto(null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
