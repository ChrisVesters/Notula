package com.cvesters.notula.event;

import java.util.List;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import com.cvesters.notula.common.controller.BaseController;
import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.event.dto.EventDto;

@Controller
public class EventWebSocket extends BaseController {

	private static final String ENDPOINT = "/meetings/{id}/events/{after}";

	private final EventService eventService;

	public EventWebSocket(final EventService eventService) {
		this.eventService = eventService;
	}

	@SubscribeMapping(ENDPOINT)
	public List<EventDto> replay(@DestinationVariable final long id,
			@DestinationVariable final long after) {
		final Principal principal = getPrincipal();

		return eventService.findAllSince(principal, id, after)
				.stream()
				.map(EventDto::new)
				.toList();
	}
}
