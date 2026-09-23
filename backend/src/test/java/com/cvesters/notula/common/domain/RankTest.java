package com.cvesters.notula.common.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class RankTest {

	private static final String DIGITS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

	private static final int REPEATS = 500;

	@Nested
	class Constructor {

		@Test
		void empty() {
			final var rank = new Rank();

			assertThat(rank.value()).isEqualTo("V");
		}

		@Test
		void explicit() {
			final var rank = new Rank("a1");
			assertThat(rank.value()).isEqualTo("a1");
		}

		@Test
		void valueNull() {
			assertThatThrownBy(() -> new Rank(null))
					.isInstanceOf(NullPointerException.class);
		}

		@ParameterizedTest
		@ValueSource(strings = { "", " ", "a-1", "0", "a0" })
		void vallueInvalid(final String value) {
			assertThatThrownBy(() -> new Rank(value))
					.isInstanceOf(IllegalArgumentException.class);
		}
	}

	@Nested
	class AfterElement {

		@Test
		void between() {
			final var first = new Rank("B");
			final var second = new Rank("D");
			final var third = new Rank("F");
			final List<Rank> elements = List.of(first, second, third);

			final Rank after = Rank.after(first, elements, Function.identity());

			assertThat(after.value()).isEqualTo("C");
		}

		@Test
		void front() {
			final var first = new Rank("B");
			final var second = new Rank("D");
			final List<Rank> elements = List.of(first, second);

			final Rank after = Rank.after(null, elements, Function.identity());

			assertThat(after.value()).isEqualTo("A");
		}

		@Test
		void last() {
			final var first = new Rank("B");
			final var second = new Rank("D");
			final var third = new Rank("F");
			final List<Rank> elements = List.of(first, second, third);

			final Rank after = Rank.after(third, elements, Function.identity());

			assertThat(after.value()).isEqualTo("G");
		}

		@Test
		void only() {
			final var first = new Rank("B");
			final List<Rank> elements = List.of(first);

			final Rank after = Rank.after(first, elements, Function.identity());

			assertThat(after.value()).isEqualTo("C");
		}

		@Test
		void empty() {
			final List<Rank> elements = List.of();

			final Rank after = Rank.after(null, elements, Function.identity());

			assertThat(after.value()).isEqualTo("V");
		}

		@Test
		void mapped() {
			final var first = new Ranked(1, new Rank("B"));
			final var second = new Ranked(2, new Rank("D"));
			final List<Ranked> elements = List.of(first, second);

			final Rank after = Rank.after(first, elements, Ranked::rank);

			assertThat(after.value()).isEqualTo("C");
		}

		@Test
		void middle() {
			final var lower = new Rank("1");
			final var upper = new Rank("z");
			final List<Rank> elements = List.of(lower, upper);

			final Rank after = Rank.after(lower, elements, Function.identity());

			assertThat(after.value()).isEqualTo("V");
		}

		@Test
		void firstDifference() {
			final var lower = new Rank("abc1");
			final var upper = new Rank("abczA4B");
			final List<Rank> elements = List.of(lower, upper);

			final Rank after = Rank.after(lower, elements, Function.identity());

			assertThat(after.value()).isEqualTo("abcV");
		}

		@Test
		void adjacent() {
			final var lower = new Rank("1");
			final var upper = new Rank("2");
			final List<Rank> elements = List.of(lower, upper);

			final Rank after = Rank.after(lower, elements, Function.identity());

			assertThat(after.value()).isEqualTo("1V");
		}

		@Test
		void adjacentPrefixed() {
			final var lower = new Rank("aaa1");
			final var upper = new Rank("aaa2");
			final List<Rank> elements = List.of(lower, upper);

			final Rank after = Rank.after(lower, elements, Function.identity());

			assertThat(after.value()).isEqualTo("aaa1V");
		}

		@Test
		void adjacentZero() {
			final var lower = new Rank("aaa1");
			final var upper = new Rank("aaa11");
			final List<Rank> elements = List.of(lower, upper);

			final Rank after = Rank.after(lower, elements, Function.identity());

			assertThat(after.value()).isEqualTo("aaa10V");
		}

		@Test
		void sharedPrefix() {
			final var lower = new Rank("aaa1");
			final var upper = new Rank("aaaz");
			final List<Rank> elements = List.of(lower, upper);

			final Rank after = Rank.after(lower, elements, Function.identity());

			assertThat(after.value()).isEqualTo("aaaV");
		}

		@Test
		void lowerIsPrefix() {
			final var lower = new Rank("a1");
			final var upper = new Rank("a1b");
			final List<Rank> elements = List.of(lower, upper);

			final Rank after = Rank.after(lower, elements, Function.identity());

			assertThat(after.value()).isEqualTo("a1I");
		}

		@Test
		void zeroes() {
			final var lower = new Rank("a1");
			final var upper = new Rank("a100z");
			final List<Rank> elements = List.of(lower, upper);

			final Rank after = Rank.after(lower, elements, Function.identity());

			assertThat(after.value()).isEqualTo("a100U");
		}

		@Test
		void lowerLonger() {
			final var lower = new Rank("a1Zdbe");
			final var upper = new Rank("a3");
			final List<Rank> elements = List.of(lower, upper);

			final Rank after = Rank.after(lower, elements, Function.identity());

			assertThat(after.value()).isEqualTo("a2");
		}

		@Test
		void lowerLongerAdjacent() {
			final var lower = new Rank("a2Zdbe");
			final var upper = new Rank("a3");
			final List<Rank> elements = List.of(lower, upper);

			final Rank after = Rank.after(lower, elements, Function.identity());

			assertThat(after.value()).isEqualTo("a2m");
		}

		@Test
		void upperLonger() {
			final var lower = new Rank("a1");
			final var upper = new Rank("a3Zdbe");
			final List<Rank> elements = List.of(lower, upper);

			final Rank after = Rank.after(lower, elements, Function.identity());

			assertThat(after.value()).isEqualTo("a2");
		}

		@Test
		void upperLongerAdjacent() {
			final var lower = new Rank("a2");
			final var upper = new Rank("a3Zdbe");
			final List<Rank> elements = List.of(lower, upper);

			final Rank after = Rank.after(lower, elements, Function.identity());

			assertThat(after.value()).isEqualTo("a2V");
		}

		@Test
		void frontLowest() {
			final var first = new Rank("1");
			final List<Rank> elements = List.of(first);

			final Rank after = Rank.after(null, elements, Function.identity());

			assertThat(after.value()).isEqualTo("0V");
		}

		@Test
		void frontLonger() {
			final var first = new Rank("VAD01a");
			final List<Rank> elements = List.of(first);

			final Rank after = Rank.after(null, elements, Function.identity());

			assertThat(after.value()).isEqualTo("VAD01Z");
		}

		@Test
		void frontPadded() {
			final var first = new Rank("VAD01");
			final List<Rank> elements = List.of(first);

			final Rank after = Rank.after(null, elements, Function.identity());

			assertThat(after.value()).isEqualTo("VAD00V");
		}

		@Test
		void lastHighest() {
			final var last = new Rank("z");
			final List<Rank> elements = List.of(last);

			final Rank after = Rank.after(last, elements, Function.identity());

			assertThat(after.value()).isEqualTo("zV");
		}

		@Test
		void lastLonger() {
			final var last = new Rank("VAD01a");
			final List<Rank> elements = List.of(last);

			final Rank after = Rank.after(last, elements, Function.identity());

			assertThat(after.value()).isEqualTo("VAD01b");
		}

		@Test
		void lastPadded() {
			final var last = new Rank("VAD0z");
			final List<Rank> elements = List.of(last);

			final Rank after = Rank.after(last, elements, Function.identity());

			assertThat(after.value()).isEqualTo("VAD0zV");
		}

		@Test
		void unordered() {
			final var lower = new Rank("b");
			final var upper = new Rank("a");
			final List<Rank> elements = List.of(lower, upper);

			assertThatThrownBy(
					() -> Rank.after(lower, elements, Function.identity()))
							.isInstanceOf(IllegalArgumentException.class);
		}

		@Test
		void duplicate() {
			final var rank = new Rank("a");
			final List<Rank> elements = List.of(rank, rank);

			assertThatThrownBy(
					() -> Rank.after(rank, elements, Function.identity()))
							.isInstanceOf(IllegalArgumentException.class);
		}

		@Test
		void elementsNull() {
			final Function<Rank, Rank> rank = Function.identity();

			assertThatThrownBy(() -> Rank.after(null, null, rank))
					.isInstanceOf(NullPointerException.class);
		}

		@Test
		void rankNull() {
			final List<Rank> elements = List.of(new Rank("B"));

			assertThatThrownBy(() -> Rank.after(null, elements, null))
					.isInstanceOf(NullPointerException.class);
		}

		private record Ranked(long id, Rank rank) {
		}
	}

	@Nested
	class CompareTo {

		@Test
		void same() {
			final var rank = new Rank("a");

			assertThat(rank.compareTo(rank)).isEqualTo(0);
		}

		@Test
		void sameLength() {
			final var rank1 = new Rank("a");
			final var rank2 = new Rank("b");

			assertThat(rank1.compareTo(rank2)).isLessThan(0);
			assertThat(rank2.compareTo(rank1)).isGreaterThan(0);
		}

		@Test
		void shorterLower() {
			final var rank1 = new Rank("a");
			final var rank2 = new Rank("aB32y");

			assertThat(rank1.compareTo(rank2)).isLessThan(0);
			assertThat(rank2.compareTo(rank1)).isGreaterThan(0);
		}

		@Test
		void shorterUpper() {
			final var rank1 = new Rank("2bDy");
			final var rank2 = new Rank("x");

			assertThat(rank1.compareTo(rank2)).isLessThan(0);
			assertThat(rank2.compareTo(rank1)).isGreaterThan(0);
		}

		@Test
		void mixedCharacters() {
			final var rank1 = new Rank("1");
			final var rank2 = new Rank("X");

			assertThat(rank1.compareTo(rank2)).isLessThan(0);
			assertThat(rank2.compareTo(rank1)).isGreaterThan(0);
		}

	}

	@Nested
	class Exhaustive {

		@Test
		void everyPairOfDigits() {
			for (int low = 1; low < DIGITS.length(); low++) {
				for (int high = low + 1; high < DIGITS.length(); high++) {
					final var lower = new Rank(
							String.valueOf(DIGITS.charAt(low)));
					final var upper = new Rank(
							String.valueOf(DIGITS.charAt(high)));
					final List<Rank> elements = List.of(lower, upper);

					final Rank middle = Rank.after(lower, elements,
							Function.identity());

					assertThat(middle).isStrictlyBetween(lower, upper);
				}
			}
		}

		@Test
		void repeatedlyBetweenTheSameTwo() {
			final var lower = new Rank("1");
			var upper = new Rank("2");

			for (int i = 0; i < REPEATS; i++) {
				final List<Rank> elements = List.of(lower, upper);

				final Rank middle = Rank.after(lower, elements,
						Function.identity());

				assertThat(middle).isStrictlyBetween(lower, upper);

				upper = middle;
			}
		}

		@Test
		void repeatedlyAtTheFront() {
			var first = new Rank();

			for (int i = 0; i < REPEATS; i++) {
				final List<Rank> elements = List.of(first);

				final Rank before = Rank.after(null, elements,
						Function.identity());

				assertThat(before).isLessThan(first);

				first = before;
			}
		}

		@Test
		void repeatedlyAtTheBack() {
			var last = new Rank();

			for (int i = 0; i < REPEATS; i++) {
				final List<Rank> elements = List.of(last);

				final Rank after = Rank.after(last, elements,
						Function.identity());

				assertThat(after).isGreaterThan(last);

				last = after;
			}
		}

		@Test
		void ordersAsItWasBuilt() {
			final List<Rank> ranks = new ArrayList<>();

			for (int i = 0; i < REPEATS; i++) {
				final int at = i % (ranks.size() + 1);
				final Rank after = at == 0 ? null : ranks.get(at - 1);

				ranks.add(at, Rank.after(after, ranks, Function.identity()));
			}

			assertThat(ranks).isSorted();
		}
	}
}
