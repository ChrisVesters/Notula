package com.cvesters.notula.organisation;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cvesters.notula.common.controller.BaseController;
import com.cvesters.notula.common.domain.Principal;
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

	@GetMapping
	public ResponseEntity<List<OrganisationInfoDto>> getAll() {
		final Principal principal = getPrincipal();

		final List<OrganisationInfo> organisations = organisationService
				.getAll(principal);
		final List<OrganisationInfoDto> dto = organisations.stream()
				.map(OrganisationInfoDto::new)
				.toList();

		return ResponseEntity.ok(dto);
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<OrganisationInfoDto> create(
			@Valid @RequestBody CreateOrganisationDto request) {
		final Principal principal = getPrincipal();

		final OrganisationInfo organisation = organisationService
				.create(principal, request.toBdo());
		final var dto = new OrganisationInfoDto(organisation);

		return ResponseEntity
				.created(getLocation("/{id}", organisation.getId()))
				.body(dto);
	}

}
