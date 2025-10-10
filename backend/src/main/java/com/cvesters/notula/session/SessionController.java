package com.cvesters.notula.session;

import jakarta.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cvesters.notula.common.controller.BaseController;
import com.cvesters.notula.session.bdo.SessionInfo;
import com.cvesters.notula.session.bdo.UserLogin;
import com.cvesters.notula.session.dto.CreateSessionDto;


@RestController
@RequestMapping("/api/sessions")
public class SessionController extends BaseController{
	
	private final SessionService sessionService;

	public SessionController(SessionService sessionService) {
		this.sessionService = sessionService;
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> create(@Valid @RequestBody final CreateSessionDto request) {	
		final UserLogin login = request.toBdo();
		final SessionInfo session = this.sessionService.create(login);

		return ResponseEntity.ok().build();
	}
}
