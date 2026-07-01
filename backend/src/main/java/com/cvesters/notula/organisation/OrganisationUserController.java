package com.cvesters.notula.organisation;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cvesters.notula.common.controller.BaseController;
import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.organisation.bdo.OrganisationUserInfo;
import com.cvesters.notula.organisation.dto.OrganisationUserInfoDto;

@RestController
@RequestMapping("/api/organisation-users")
public class OrganisationUserController extends BaseController {

	private final OrganisationUserService organisationUserService;

	public OrganisationUserController(
			final OrganisationUserService organisationUserService) {
		this.organisationUserService = organisationUserService;
	}

	@GetMapping
	public ResponseEntity<List<OrganisationUserInfoDto>> getAll() {
		final Principal principal = getPrincipal();

		final List<OrganisationUserInfo> users = organisationUserService
				.getAllForOrganisation(principal);
		final List<OrganisationUserInfoDto> dto = users.stream()
				.map(OrganisationUserInfoDto::new)
				.toList();

		return ResponseEntity.ok(dto);
	}

}
