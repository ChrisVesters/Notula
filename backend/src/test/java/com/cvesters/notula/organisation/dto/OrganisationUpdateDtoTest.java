package com.cvesters.notula.organisation.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.cvesters.notula.organisation.TestOrganisation;

class OrganisationUpdateDtoTest {

	private static final TestOrganisation ORGANISATION = TestOrganisation.SPORER;

	@Test
	void toBdo() {
		final var dto = new OrganisationUpdateDto(ORGANISATION.getName());
		final var bdo = dto.toBdo();

		assertThatThrownBy(bdo::getId)
				.isInstanceOf(IllegalStateException.class);
		assertThat(bdo.getName()).isEqualTo(ORGANISATION.getName());
	}
}
