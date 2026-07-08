package com.cvesters.notula.organisation;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cvesters.notula.common.controller.BaseController;
import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.organisation.bdo.OrganisationUserAction;
import com.cvesters.notula.organisation.bdo.OrganisationUserInfo;
import com.cvesters.notula.organisation.bdo.OrganisationUserView;
import com.cvesters.notula.organisation.dto.OrganisationUserActionDto;
import com.cvesters.notula.organisation.dto.OrganisationUserInfoDto;
import com.cvesters.notula.organisation.dto.OrganisationUserViewDto;

@RestController
@RequestMapping("/api/organisation-users")
public class OrganisationUserController extends BaseController {

	private final OrganisationUserService organisationUserService;

	public OrganisationUserController(
			final OrganisationUserService organisationUserService) {
		this.organisationUserService = organisationUserService;
	}

	@GetMapping
	public ResponseEntity<List<OrganisationUserViewDto>> getAll() {
		final Principal principal = getPrincipal();

		final List<OrganisationUserView> users = organisationUserService
				.getAllForOrganisation(principal);
		final List<OrganisationUserViewDto> dto = users.stream()
				.map(OrganisationUserViewDto::new)
				.toList();

		return ResponseEntity.ok(dto);
	}

	@PostMapping
	public ResponseEntity<OrganisationUserInfoDto> create(
			@Valid @RequestBody OrganisationUserActionDto.Create request) {
		final Principal principal = getPrincipal();

		final OrganisationUserAction.Create action = request.toBdo();
		final OrganisationUserInfo created = organisationUserService
				.create(principal, action);
		final var dto = new OrganisationUserInfoDto(created);

		return ResponseEntity.created(getLocation("/{id}", dto.id())).body(dto);
	}

}
