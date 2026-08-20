package com.cvesters.notula.textblock;

import jakarta.validation.Valid;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import com.cvesters.notula.common.controller.BaseController;
import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.textblock.bdo.TextBlockAction;
import com.cvesters.notula.textblock.dto.TextBlockActionDto;

@Controller
public class TextBlockWebSocket extends BaseController {

	private static final String ENDPOINT = "/text-blocks";

	private TextBlockService textBlockService;

	public TextBlockWebSocket(final TextBlockService textBlockService) {
		this.textBlockService = textBlockService;
	}

	@MessageMapping(ENDPOINT + "/update")
	public void update(@Valid @Payload final TextBlockActionDto.Update dto) {
		final Principal principal = getPrincipal();

		final long meetingId = dto.getMeetingId();
		final long blockId = dto.getBlockId();
		final TextBlockAction.Update action = dto.toBdo();
		textBlockService.update(principal, meetingId, blockId, action);
	}
}
