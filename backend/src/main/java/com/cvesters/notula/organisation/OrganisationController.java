package com.cvesters.notula.organisation;

import jakarta.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cvesters.notula.organisation.bdo.Organisation;
import com.cvesters.notula.organisation.dto.CreateOrganisationDto;
import com.cvesters.notula.organisation.dto.OrganisationDto;

@RestController
@RequestMapping("/api/organisations")
public class OrganisationController {

	private final OrganisationService organisationService;

	public OrganisationController(final OrganisationService organisationService) {
		this.organisationService = organisationService;
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<String> create(@Valid @RequestBody CreateOrganisationDto request) {
		final Organisation created = organisationService.create(request.toBdo());
		final OrganisationDto dto = OrganisationDto.fromBdo(created);

		return ResponseEntity.created(null)
			.body("Hello World!");
	}

}
