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
import com.cvesters.notula.session.dto.SessionInfoDto;
import com.cvesters.notula.user.bdo.UserLogin;

@RestController
@RequestMapping("/api/sessions")
public class SessionController extends BaseController {

	private final SessionService sessionService;

	public SessionController(SessionService sessionService) {
		this.sessionService = sessionService;
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<SessionInfoDto> create(
			@Valid @RequestBody final CreateSessionDto request) {
		final UserLogin login = request.toBdo();
		final SessionTokens session = sessionService.create(login);
		final var dto = new SessionInfoDto(session);

		// TODO: set refresh token as http secure cookie only
	// 	    ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
    //     .httpOnly(true)
    //     .secure(true)
    //     .path("/api/auth/refresh")
    //     .maxAge(Duration.ofDays(7))
    //     .build();
    // response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

		return ResponseEntity
				.created(ServletUriComponentsBuilder.fromCurrentRequest()
						.path("/{id}")
						.buildAndExpand(session.getId())
						.toUri())
				.body(dto);
	}
}
