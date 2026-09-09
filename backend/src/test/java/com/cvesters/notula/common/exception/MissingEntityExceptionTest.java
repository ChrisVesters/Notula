package com.cvesters.notula.common.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MissingEntityExceptionTest {

	@Nested
	class Constructor {

		@Test
		void success() {
			final var exception = new MissingEntityException();

			assertThat(exception).isInstanceOf(RuntimeException.class);
			assertThat(exception.getMessage()).isNull();
			assertThat(exception.getCause()).isNull();
		}
	}
}
