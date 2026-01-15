package com.cvesters.notula.organisation.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.cvesters.notula.organisation.TestOrganisation;

class OrganisationCreateDtoTest {

	private static final TestOrganisation ORGANISATION = TestOrganisation.SPORER;

	@Test
	void toBdo() {
		final var dto = new OrganisationCreateDto(ORGANISATION.getName());
		final var bdo = dto.toBdo();
		
		assertThat(bdo.getId()).isNull();
		assertThat(bdo.getName()).isEqualTo(ORGANISATION.getName());
	}
}
