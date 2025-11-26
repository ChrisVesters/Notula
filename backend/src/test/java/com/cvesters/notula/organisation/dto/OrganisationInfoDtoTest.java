package com.cvesters.notula.organisation.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.organisation.TestOrganisation;

class OrganisationInfoDtoTest {

	private static final TestOrganisation ORGANISATION = TestOrganisation.SPORER;

	@Nested
	class Constructor {

		@Test
		void success() {
			final var bdo = ORGANISATION.info();

			final var dto = new OrganisationInfoDto(bdo);

			assertThat(dto.id()).isEqualTo(ORGANISATION.getId());
			assertThat(dto.name()).isEqualTo(ORGANISATION.getName());
		}

		@Test
		void organisationNull() {
			assertThatThrownBy(() -> new OrganisationInfoDto(null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
