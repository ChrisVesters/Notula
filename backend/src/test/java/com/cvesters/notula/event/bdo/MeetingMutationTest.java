package com.cvesters.notula.event.bdo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.common.domain.Splice;

class MeetingMutationTest {

	private static final Splice EDIT = new Splice(4, 12, "Updated");

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
			final var mutation = new MeetingMutation.Rename(EDIT);

			assertThat(mutation.edit()).isEqualTo(EDIT);
		}

		@Test
		void editNull() {
			assertThatThrownBy(() -> new MeetingMutation.Rename(null))
					.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class Describe {

		@Test
		void success() {
			final var mutation = new MeetingMutation.Describe(EDIT);

			assertThat(mutation.edit()).isEqualTo(EDIT);
		}

		@Test
		void editNull() {
			assertThatThrownBy(() -> new MeetingMutation.Describe(null))
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
