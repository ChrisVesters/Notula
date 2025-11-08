package com.cvesters.notula.session;

import jakarta.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.cvesters.notula.common.controller.BaseController;
import com.cvesters.notula.session.bdo.SessionTokens;
import com.cvesters.notula.session.dto.CreateSessionDto;
import com.cvesters.notula.user.bdo.UserLogin;

@RestController
@RequestMapping("/api/sessions")
public class SessionController extends BaseController {

	private final SessionService sessionService;

	public SessionController(SessionService sessionService) {
		this.sessionService = sessionService;
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> create(
			@Valid @RequestBody final CreateSessionDto request) {
		final UserLogin login = request.toBdo();
		// TODO: Should be access tokens!
		// TODO: well, we also want to return the session info.
		final SessionTokens session = sessionService.create(login);

		return ResponseEntity
				.created(ServletUriComponentsBuilder.fromCurrentRequest()
						.path("/{id}")
						.buildAndExpand(1)
						.toUri())
				.body("{}");
	}
}
