package com.cvesters.notula.event.bdo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MeetingMutationTest {

	@Nested
	class Add {

		@Test
		void success() {
			final var mutation = new MeetingMutation.Add("Kickoff");

			assertThat(mutation.name()).isEqualTo("Kickoff");
		}

		@Test
		void nameNull() {
			assertThatThrownBy(() -> new MeetingMutation.Add(null))
					.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class Rename {

		@Test
		void success() {
			final var mutation = new MeetingMutation.Rename(4, 12, "Updated");

			assertThat(mutation.position()).isEqualTo(4);
			assertThat(mutation.length()).isEqualTo(12);
			assertThat(mutation.value()).isEqualTo("Updated");
		}

		@Test
		void start() {
			final var mutation = new MeetingMutation.Rename(0, 12, "Updated");

			assertThat(mutation.position()).isEqualTo(0);
		}

		@Test
		void insert() {
			final var mutation = new MeetingMutation.Rename(4, 0, "Updated");

			assertThat(mutation.length()).isEqualTo(0);
		}

		@Test
		void positionNegative() {
			assertThatThrownBy(() -> new MeetingMutation.Rename(-1, 12, "Up"))
					.isInstanceOf(IllegalArgumentException.class);
		}

		@Test
		void lengthNegative() {
			assertThatThrownBy(() -> new MeetingMutation.Rename(4, -1, "Up"))
					.isInstanceOf(IllegalArgumentException.class);
		}

		@Test
		void valueNull() {
			assertThatThrownBy(() -> new MeetingMutation.Rename(4, 12, null))
					.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class Describe {

		@Test
		void success() {
			final var mutation = new MeetingMutation.Describe(4, 12,
					"Updated");

			assertThat(mutation.position()).isEqualTo(4);
			assertThat(mutation.length()).isEqualTo(12);
			assertThat(mutation.value()).isEqualTo("Updated");
		}

		@Test
		void start() {
			final var mutation = new MeetingMutation.Describe(0, 12,
					"Updated");

			assertThat(mutation.position()).isEqualTo(0);
		}

		@Test
		void insert() {
			final var mutation = new MeetingMutation.Describe(4, 0, "Updated");

			assertThat(mutation.length()).isEqualTo(0);
		}

		@Test
		void positionNegative() {
			assertThatThrownBy(
					() -> new MeetingMutation.Describe(-1, 12, "Up"))
							.isInstanceOf(IllegalArgumentException.class);
		}

		@Test
		void lengthNegative() {
			assertThatThrownBy(() -> new MeetingMutation.Describe(4, -1, "Up"))
					.isInstanceOf(IllegalArgumentException.class);
		}

		@Test
		void valueNull() {
			assertThatThrownBy(() -> new MeetingMutation.Describe(4, 12, null))
					.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class Remove {

		@Test
		void success() {
			final var mutation = new MeetingMutation.Remove();

			assertThat(mutation).isEqualTo(new MeetingMutation.Remove());
		}
	}
}
