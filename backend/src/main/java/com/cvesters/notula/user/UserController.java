package com.cvesters.notula.user;

import jakarta.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.cvesters.notula.common.controller.BaseController;
import com.cvesters.notula.common.domain.Password;
import com.cvesters.notula.credential.CredentialService;
import com.cvesters.notula.credential.bdo.CredentialAction;
import com.cvesters.notula.user.bdo.UserInfo;
import com.cvesters.notula.user.dto.UserCreateDto;
import com.cvesters.notula.user.dto.UserInfoDto;

@RestController
@RequestMapping("/api/users")
public class UserController extends BaseController {

	private final UserService userService;
	private final CredentialService credentialService;

	public UserController(final UserService userService,
			final CredentialService credentialService) {
		this.userService = userService;
		this.credentialService = credentialService;
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<UserInfoDto> create(
			@Valid @RequestBody final UserCreateDto request) {

		final UserInfo userAction = request.toBdo();
		final UserInfo userInfo = userService.findByEmail(userAction.getEmail())
				.orElseGet(() -> userService.createUser(userAction));

		final var credentialAction = new CredentialAction.Create(
				userInfo.getId(), new Password(request.password()));
		credentialService.create(credentialAction);

		final var response = new UserInfoDto(userInfo);

		return ResponseEntity
				.created(ServletUriComponentsBuilder.fromCurrentRequest()
						.path("/{id}")
						.buildAndExpand(userInfo.getId())
						.toUri())
				.body(response);
	}

}
