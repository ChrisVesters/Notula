package com.cvesters.notula.textblock;

import jakarta.validation.Valid;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import com.cvesters.notula.common.controller.BaseController;
import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.textblock.bdo.TextBlockAction;
import com.cvesters.notula.textblock.dto.TextBlockActionDto;

@Controller
public class TextBlockWebSocket extends BaseController {

	private static final String BASE_ENDPOINT = "/meetings/{meetingId}/topics/{topicId}/text-blocks";

	private TextBlockService textBlockService;

	public TextBlockWebSocket(final TextBlockService textBlockService) {
		this.textBlockService = textBlockService;
	}

	@MessageMapping(BASE_ENDPOINT + "/{blockId}")
	public void update(@DestinationVariable final long meetingId,
			@DestinationVariable final long topicId,
			@DestinationVariable final long blockId,
			@Valid @Payload final TextBlockActionDto.Update dto) {
		final Principal principal = getPrincipal();

		final TextBlockAction.Update action = dto.toBdo();
		textBlockService.update(principal, meetingId, topicId, blockId, action);
	}
}
