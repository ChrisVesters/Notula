package com.cvesters.notula.topic;

import jakarta.validation.Valid;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import com.cvesters.notula.common.controller.BaseController;
import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.topic.bdo.TopicAction;
import com.cvesters.notula.topic.dto.TopicActionDto;

@Controller
public class TopicWebSocket extends BaseController {

	private static final String ENDPOINT = "/topics";

	private TopicService topicService;

	public TopicWebSocket(final TopicService topicService) {
		this.topicService = topicService;
	}

	@MessageMapping(ENDPOINT + "/create")
	public void create(@Valid @Payload final TopicActionDto.Create dto) {
		final Principal principal = getPrincipal();

		final TopicAction.Create action = dto.toBdo();
		topicService.create(principal, action);
	}

	@MessageMapping(ENDPOINT + "/update")
	public void update(@Valid @Payload final TopicActionDto.Update dto) {
		final Principal principal = getPrincipal();

		final long meetingId = dto.getMeetingId();
		final long topicId = dto.getTopicId();
		final TopicAction.Update action = dto.toBdo();
		topicService.update(principal, meetingId, topicId, action);
	}

	@MessageMapping(ENDPOINT + "/delete")
	public void delete(@Valid @Payload final TopicActionDto.Delete dto) {
		final Principal principal = getPrincipal();

		final long meetingId = dto.getMeetingId();
		final long topicId = dto.getTopicId();
		topicService.delete(principal, meetingId, topicId);
	}
}
