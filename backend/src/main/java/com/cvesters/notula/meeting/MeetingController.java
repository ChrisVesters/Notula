package com.cvesters.notula.meeting;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cvesters.notula.common.controller.BaseController;
import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.meeting.bdo.MeetingInfo;

@RestController
@RequestMapping("/api/meetings")
public class MeetingController extends BaseController {

	private final MeetingService meetingService;

	public MeetingController(final MeetingService meetingService) {
		this.meetingService = meetingService;
	}

	@GetMapping
	public ResponseEntity<List<MeetingInfo>> getAll() {
		final Principal principal = getPrincipal();

		final List<MeetingInfo> meetings = meetingService.getAll(principal);

		return ResponseEntity.ok(meetings);
	}
}
