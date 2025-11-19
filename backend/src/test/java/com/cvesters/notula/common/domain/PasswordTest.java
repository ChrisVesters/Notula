package com.cvesters.notula.common.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class PasswordTest {

	@Nested
	class Constructor {

		@Test
		void valueNull() {
			assertThatThrownBy(() -> new Password(null))
					.isInstanceOf(NullPointerException.class);
		}

		@ParameterizedTest
		@EmptySource
		@ValueSource(strings = { "a", "1234567", "@bC_d~" })
		void invalid(final String value) {
			assertThatThrownBy(() -> new Password(value))
					.isInstanceOf(IllegalArgumentException.class);
		}

		@ParameterizedTest
		@ValueSource(strings = {"12345678", "abcdefgh", "a_@b%D~ë", "        "})
		void valid(final String value) {
			final var password = new Password(value);

			assertThat(password).isNotNull();
		}
	}

	@Nested
	class Value {

		@ParameterizedTest
		@ValueSource(strings = {"12345678", "abcdefgh", "a_@b%D~ë"})
		void valid(final String value) {
			final var password = new Password(value);

			assertThat(password.value()).isEqualTo(value);
		}
	}
}
