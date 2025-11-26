package com.cvesters.notula.organisation;

import jakarta.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cvesters.notula.common.controller.BaseController;
import com.cvesters.notula.organisation.bdo.OrganisationInfo;
import com.cvesters.notula.organisation.dto.CreateOrganisationDto;
import com.cvesters.notula.organisation.dto.OrganisationInfoDto;

@RestController
@RequestMapping("/api/organisations")
public class OrganisationController extends BaseController {

	private final OrganisationService organisationService;

	public OrganisationController(
			final OrganisationService organisationService) {
		this.organisationService = organisationService;
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<OrganisationInfoDto> create(
			@Valid @RequestBody CreateOrganisationDto request) {
		final OrganisationInfo organisation = organisationService
				.create(request.toBdo());
		final var dto = new OrganisationInfoDto(organisation);

		return ResponseEntity
				.created(getLocation("/{id}", organisation.getId()))
				.body(dto);
	}

}
