package com.cvesters.notula.topic;

import jakarta.validation.Valid;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import com.cvesters.notula.common.domain.Origin;
import com.cvesters.notula.topic.bdo.TopicAction;
import com.cvesters.notula.topic.dto.TopicActionDto;

@Controller
public class TopicWebSocket {

	private static final String ENDPOINT = "/topics";

	private TopicService topicService;

	public TopicWebSocket(final TopicService topicService) {
		this.topicService = topicService;
	}

	@MessageMapping(ENDPOINT + "/create")
	public void create(final Origin origin,
			@Valid @Payload final TopicActionDto.Create dto) {
		final TopicAction.Create action = dto.toBdo();
		topicService.create(origin, action);
	}

	@MessageMapping(ENDPOINT + "/move")
	public void move(final Origin origin,
			@Valid @Payload final TopicActionDto.Move dto) {
		final long topicId = dto.getTopicId();
		final TopicAction.Move action = dto.toBdo();
		topicService.move(origin, topicId, action);
	}

	@MessageMapping(ENDPOINT + "/update")
	public void update(final Origin origin,
			@Valid @Payload final TopicActionDto.Update dto) {
		final long topicId = dto.getTopicId();
		final TopicAction.Update action = dto.toBdo();
		topicService.update(origin, topicId, action);
	}

	@MessageMapping(ENDPOINT + "/delete")
	public void delete(final Origin origin,
			@Valid @Payload final TopicActionDto.Delete dto) {
		final long topicId = dto.getTopicId();
		topicService.delete(origin, topicId);
	}
}
