package com.cvesters.notula.meeting;

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
import com.cvesters.notula.meeting.bdo.MeetingInfo;
import com.cvesters.notula.meeting.dto.MeetingCreateDto;
import com.cvesters.notula.meeting.dto.MeetingInfoDto;

@RestController
@RequestMapping("/api/meetings")
public class MeetingController extends BaseController {

	private final MeetingService meetingService;

	public MeetingController(final MeetingService meetingService) {
		this.meetingService = meetingService;
	}

	@GetMapping
	public ResponseEntity<List<MeetingInfoDto>> getAll() {
		final Principal principal = getPrincipal();

		final List<MeetingInfoDto> dto = meetingService.getAll(principal)
				.stream()
				.map(MeetingInfoDto::new)
				.toList();

		return ResponseEntity.ok(dto);
	}

	@PostMapping
	public ResponseEntity<MeetingInfoDto> create(
			@Valid @RequestBody MeetingCreateDto request) {
		final Principal principal = getPrincipal();

		final MeetingInfo bdo = request.toBdo(principal.organisationId());
		final MeetingInfo created = meetingService.create(principal, bdo);
		final var dto = new MeetingInfoDto(created);

		return ResponseEntity
				.created(getLocation("/{id}", dto.id()))
				.body(dto);
	}

}
