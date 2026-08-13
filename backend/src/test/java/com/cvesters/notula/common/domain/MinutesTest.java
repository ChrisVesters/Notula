package com.cvesters.notula.common.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MinutesTest {

	@Nested
	class Constructor {

		@ParameterizedTest
		@ValueSource(ints = { 0, 1, 60, Integer.MAX_VALUE })
		void valid(final int value) {
			final var minutes = new Minutes(value);

			assertThat(minutes).isNotNull();
			assertThat(minutes.value()).isEqualTo(value);
		}

		@ParameterizedTest
		@ValueSource(ints = { Integer.MIN_VALUE, -60, -1 })
		void negative(final int value) {
			assertThatThrownBy(() -> new Minutes(value))
					.isInstanceOf(IllegalArgumentException.class);
		}

	}
}
