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
import com.cvesters.notula.user.bdo.UserInfo;
import com.cvesters.notula.user.dto.CreateUserDto;
import com.cvesters.notula.user.dto.UserInfoDto;

@RestController
@RequestMapping("/api/users")
public class UserController extends BaseController {

	private final UserService userService;

	public UserController(final UserService userService) {
		this.userService = userService;
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<UserInfoDto> create(
			@Valid @RequestBody final CreateUserDto request) {

		final UserInfo user = userService.createUser(request.toBdo());
		final var response = new UserInfoDto(user);

		return ResponseEntity
				.created(ServletUriComponentsBuilder.fromCurrentRequest()
						.path("/{id}")
						.buildAndExpand(user.getId())
						.toUri())
				.body(response);
	}

}
