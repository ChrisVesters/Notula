package com.cvesters.notula.topic;

import jakarta.validation.Valid;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import com.cvesters.notula.common.controller.BaseController;
import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.topic.bdo.TopicAction;
import com.cvesters.notula.topic.dto.TopicActionDto;

@Controller
public class TopicWebSocket extends BaseController {

	private static final String BASE_ENDPOINT = "/meetings/{meetingId}/topics";

	private TopicService topicService;

	public TopicWebSocket(final TopicService topicService) {
		this.topicService = topicService;
	}

	@MessageMapping(BASE_ENDPOINT)
	public void create(@DestinationVariable final long meetingId,
			@Valid @Payload final TopicActionDto.Create dto) {
		final Principal principal = getPrincipal();

		final TopicAction.Create action = dto.toBdo();
		topicService.create(principal, meetingId, action);
	}

	@MessageMapping(BASE_ENDPOINT + "/{topicId}")
	public void update(@DestinationVariable final long meetingId,
			@DestinationVariable final long topicId,
			@Valid @Payload final TopicActionDto.Update dto) {
		final Principal principal = getPrincipal();

		final TopicAction.Update action = dto.toBdo();
		topicService.update(principal, meetingId, topicId, action);
	}

}
