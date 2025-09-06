package com.cvesters.notula.organisation.dto;

import jakarta.validation.constraints.NotBlank;

import com.cvesters.notula.organisation.bdo.Organisation;

public record CreateOrganisationDto(
		@NotBlank String name) {

	public Organisation toBdo() {
		return new Organisation(name);
	}
}
