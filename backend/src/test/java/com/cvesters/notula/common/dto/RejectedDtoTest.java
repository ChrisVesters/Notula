package com.cvesters.notula.common.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.common.domain.ChangeId;

import tools.jackson.databind.ObjectMapper;

class RejectedDtoTest {

	private static final UUID ID = UUID
			.fromString("7c6f0d54-2f70-4a1e-9f5a-1d4c8b2e0a11");

	private static final ChangeId CHANGE = new ChangeId(ID);

	@Nested
	class Constructor {

		@Test
		void success() {
			final var rejected = new RejectedDto(ID, true, "Try again.");

			assertThat(rejected.id()).isEqualTo(ID);
			assertThat(rejected.retryable()).isTrue();
			assertThat(rejected.reason()).isEqualTo("Try again.");
		}

		@Test
		void withoutId() {
			final var rejected = new RejectedDto(null, false, "Try again.");

			assertThat(rejected.id()).isNull();
			assertThat(rejected.retryable()).isFalse();
			assertThat(rejected.reason()).isEqualTo("Try again.");
		}

		@Test
		void reasonNull() {
			final var rejected = new RejectedDto(ID, true, null);

			assertThat(rejected.id()).isEqualTo(ID);
			assertThat(rejected.retryable()).isTrue();
			assertThat(rejected.reason()).isNull();
		}
	}

	@Nested
	class Permanent {

		@Test
		void success() {
			final var rejected = RejectedDto.permanent(CHANGE,
					"It was not valid.");

			assertThat(rejected.id()).isEqualTo(ID);
			assertThat(rejected.retryable()).isFalse();
			assertThat(rejected.reason()).isEqualTo("It was not valid.");
		}

		@Test
		void unnamed() {
			final var rejected = RejectedDto.permanent(ChangeId.NONE,
					"It was not valid.");

			assertThat(rejected.id()).isNull();
			assertThat(rejected.retryable()).isFalse();
			assertThat(rejected.reason()).isEqualTo("It was not valid.");
		}

		@Test
		void reasonNull() {
			final var rejected = RejectedDto.permanent(CHANGE, null);

			assertThat(rejected.id()).isEqualTo(ID);
			assertThat(rejected.retryable()).isFalse();
			assertThat(rejected.reason()).isNull();
		}

		@Test
		void changeIdNull() {
			assertThatThrownBy(
					() -> RejectedDto.permanent(null, "It was not valid"))
							.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class Temporary {

		@Test
		void success() {
			final var rejected = RejectedDto.temporary(CHANGE, "Try again.");

			assertThat(rejected.id()).isEqualTo(ID);
			assertThat(rejected.retryable()).isTrue();
			assertThat(rejected.reason()).isEqualTo("Try again.");
		}

		@Test
		void unnamed() {
			final var rejected = RejectedDto.temporary(ChangeId.NONE,
					"Try again.");

			assertThat(rejected.id()).isNull();
			assertThat(rejected.retryable()).isTrue();
			assertThat(rejected.reason()).isEqualTo("Try again.");
		}

		@Test
		void reasonNull() {
			final var rejected = RejectedDto.temporary(CHANGE, null);

			assertThat(rejected.id()).isEqualTo(ID);
			assertThat(rejected.retryable()).isTrue();
			assertThat(rejected.reason()).isNull();
		}

		@Test
		void changeIdNull() {
			assertThatThrownBy(
					() -> RejectedDto.temporary(null, "It was not valid"))
							.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class Serialise {

		private static final ObjectMapper MAPPER = new ObjectMapper();

		@Test
		void success() {
			final var rejected = new RejectedDto(ID, true, "Try again.");

			final String json = MAPPER.writeValueAsString(rejected);

			assertThat(json).isEqualToIgnoringWhitespace("""
					{
						"id": "7c6f0d54-2f70-4a1e-9f5a-1d4c8b2e0a11",
						"retryable": true,
						"reason": "Try again."
					}
					""");
		}

		@Test
		void withoutId() {
			final var rejected = new RejectedDto(null, false, "Try again.");

			final String json = MAPPER.writeValueAsString(rejected);

			assertThat(json).isEqualToIgnoringWhitespace("""
					{
						"id": null,
						"retryable": false,
						"reason": "Try again."
					}
					""");
		}

		@Test
		void reasonNull() {
			final var rejected = new RejectedDto(ID, true, null);

			final String json = MAPPER.writeValueAsString(rejected);

			assertThat(json).isEqualToIgnoringWhitespace("""
					{
						"id": "7c6f0d54-2f70-4a1e-9f5a-1d4c8b2e0a11",
						"retryable": true,
						"reason": null
					}
					""");
		}
	}
}
