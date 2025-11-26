package com.cvesters.notula.organisation.bdo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.cvesters.notula.organisation.TestOrganisation;

class OrganisationInfoTest {

	private final TestOrganisation ORGANISATION = TestOrganisation.SPORER;

	@Nested
	class Constructor {

		@Test
		void withoutId() {
			final var organisation = new OrganisationInfo(
					ORGANISATION.getName());

			assertThat(organisation.getId()).isNull();
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

			assertThat(organisation.getId()).isNull();
			assertThat(organisation.getName())
					.isEqualTo(ORGANISATION.getName());
		}

		@Test
		void nameNull() {
			final long id = ORGANISATION.getId();
			assertThatThrownBy(() -> new OrganisationInfo(id, null));
		}

		@ParameterizedTest
		@ValueSource(strings = { "", " ", "    " })
		void nameBlank(final String name) {
			final long id = ORGANISATION.getId();
			assertThatThrownBy(() -> new OrganisationInfo(id, name))
					.isInstanceOf(IllegalArgumentException.class);
		}
	}
}
