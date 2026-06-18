package com.cvesters.notula.organisation.bdo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.cvesters.notula.organisation.TestOrganisation;

class OrganisationInfoTest {

	private static final TestOrganisation ORGANISATION = TestOrganisation.SPORER;

	@Nested
	class Constructor {

		@Test
		void withoutId() {
			final var organisation = new OrganisationInfo(
					ORGANISATION.getName());

			assertThatThrownBy(organisation::getId)
					.isInstanceOf(IllegalStateException.class);
			assertThat(organisation.getName())
					.isEqualTo(ORGANISATION.getName());
		}

		@Test
		void withId() {
			final var organisation = new OrganisationInfo(ORGANISATION.getId(),
					ORGANISATION.getName());

			assertThat(organisation.getId()).isEqualTo(ORGANISATION.getId());
			assertThat(organisation.getName())
					.isEqualTo(ORGANISATION.getName());
		}

		@Test
		void idNull() {
			final var organisation = new OrganisationInfo(null,
					ORGANISATION.getName());

			assertThatThrownBy(organisation::getId)
					.isInstanceOf(IllegalStateException.class);
			assertThat(organisation.getName())
					.isEqualTo(ORGANISATION.getName());
		}

		@Test
		void nameNull() {
			final long id = ORGANISATION.getId();
			assertThatThrownBy(() -> new OrganisationInfo(id, null))
					.isInstanceOf(NullPointerException.class);
		}

		@ParameterizedTest
		@ValueSource(strings = { "", " ", "    " })
		void nameBlank(final String name) {
			final long id = ORGANISATION.getId();
			assertThatThrownBy(() -> new OrganisationInfo(id, name))
					.isInstanceOf(IllegalArgumentException.class);
		}
	}

	@Nested
	class Update {

		private final OrganisationInfo info = ORGANISATION.info();

		@Test
		void success() {
			final String name = "New name";
			final var update = new OrganisationInfo(name);

			info.update(update);

			assertThat(info.getName()).isEqualTo(name);
		}

		@Test
		void updateNull() {
			assertThatThrownBy(() -> info.update(null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
