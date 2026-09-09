package com.cvesters.notula.common.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class BusyEntityExceptionTest {

	@Nested
	class Constructor {

		@Test
		void success() {
			final var exception = new BusyEntityException("Locked");

			assertThat(exception.getMessage()).isEqualTo("Locked");
			assertThat(exception.getCause()).isNull();
		}

		@Test
		void withCause() {
			final var cause = new IllegalStateException("Timed out");

			final var exception = new BusyEntityException("Locked", cause);

			assertThat(exception.getMessage()).isEqualTo("Locked");
			assertThat(exception.getCause()).isSameAs(cause);
		}
	}
}
