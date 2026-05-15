package com.cvesters.notula.details;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import com.cvesters.notula.common.controller.BaseController;
import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.details.bdo.MeetingDetails;
import com.cvesters.notula.details.dto.MeetingDetailsDto;

@Controller
public class DetailsWebSocket extends BaseController {

	private static final String ENDPOINT = "/meetings/{id}";

	private final DetailsService detailsService;

	public DetailsWebSocket(final DetailsService detailsService) {
		this.detailsService = detailsService;
	}

	@SubscribeMapping(ENDPOINT)
	public MeetingDetailsDto subscribe(@DestinationVariable final long id) {
		final Principal principal = getPrincipal();

		final MeetingDetails details = detailsService.get(principal, id);
		return new MeetingDetailsDto(details);
	}
}
