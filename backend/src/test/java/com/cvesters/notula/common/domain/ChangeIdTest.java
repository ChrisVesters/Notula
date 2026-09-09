package com.cvesters.notula.common.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ChangeIdTest {

	private static final UUID VALUE = UUID
			.fromString("7c6f0d54-2f70-4a1e-9f5a-1d4c8b2e0a11");

	@Nested
	class Constructor {

		@Test
		void success() {
			final var change = new ChangeId(VALUE);

			assertThat(change.value()).isEqualTo(VALUE);
		}

		@Test
		void valueNull() {
			final var change = new ChangeId(null);

			assertThat(change.value()).isNull();
		}
	}

	@Nested
	class None {

		@Test
		void value() {
			assertThat(ChangeId.NONE.value()).isNull();
		}

		@Test
		void equals() {
			assertThat(ChangeId.NONE).isEqualTo(new ChangeId(null));
		}
	}
}
