package com.cvesters.notula.organisation.dto;

import jakarta.validation.constraints.NotBlank;

import com.cvesters.notula.organisation.bdo.OrganisationInfo;

public record OrganisationCreateDto(
		@NotBlank String name) {

	public OrganisationInfo toBdo() {
		return new OrganisationInfo(name);
	}
}
