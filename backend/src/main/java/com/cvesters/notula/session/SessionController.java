package com.cvesters.notula.session;

import java.time.Duration;
import java.time.OffsetDateTime;

import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.cvesters.notula.common.controller.BaseController;
import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.session.bdo.SessionTokens;
import com.cvesters.notula.session.bdo.SessionUpdate;
import com.cvesters.notula.session.dto.SessionCreateDto;
import com.cvesters.notula.session.dto.SessionInfoDto;
import com.cvesters.notula.session.dto.SessionUpdateDto;
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
			@Valid @RequestBody final SessionCreateDto request) {
		final UserLogin login = request.toBdo();
		final SessionTokens session = sessionService.create(login);
		final var dto = new SessionInfoDto(session);

		final ResponseCookie refreshCookie = setRefreshCookie(session);

		return ResponseEntity
				.created(ServletUriComponentsBuilder.fromCurrentRequest()
						.path("/{id}")
						.buildAndExpand(session.getId())
						.toUri())
				.header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
				.body(dto);
	}

	@PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<SessionInfoDto> update(@PathVariable final long id,
			@Valid @RequestBody final SessionUpdateDto request) {
		final Principal principal = getPrincipal();

		final SessionUpdate update = request.toBdo();
		final SessionTokens session = sessionService.update(principal, id,
				update);
		final var dto = new SessionInfoDto(session);

		return ResponseEntity.ok(dto);
	}

	@PostMapping(path = "/{id}/refresh")
	public ResponseEntity<SessionInfoDto> refresh(@PathVariable final long id,
			@CookieValue final String refreshToken) {
		final SessionTokens session = sessionService.refresh(id, refreshToken);
		final var dto = new SessionInfoDto(session);

		final ResponseCookie refreshCookie = setRefreshCookie(session);

		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
				.body(dto);
	}

	@DeleteMapping(path = "/{id}")
	public ResponseEntity<Void> delete(@PathVariable final long id) {
		final Principal principal = getPrincipal();
		sessionService.delete(principal, id);

		final ResponseCookie refreshCookie = clearRefreshCookie(id);

		return ResponseEntity.noContent()
				.header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
				.build();
	}

	private static ResponseCookie clearRefreshCookie(final long sessionId) {
		return refreshCookie(sessionId, null, Duration.ZERO);
	}

	private static ResponseCookie setRefreshCookie(
			final SessionTokens sessionTokens) {
		final long sessionId = sessionTokens.getId();
		final String refreshToken = sessionTokens.getRefreshToken()
				.orElse(null);
		final var maxAge = Duration.between(OffsetDateTime.now(),
				sessionTokens.getActiveUntil());
		return refreshCookie(sessionId, refreshToken, maxAge);

	}

	private static ResponseCookie refreshCookie(final long sessionId,
			final String refreshToken, final Duration maxAge) {
		return ResponseCookie.from("refreshToken", refreshToken)
				.httpOnly(true)
				.secure(true)
				.sameSite("None")
				.path("/api/sessions/" + sessionId + "/refresh")
				.maxAge(maxAge)
				.build();
	}
}
