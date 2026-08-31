package com.cvesters.notula.textblock;

import jakarta.validation.Valid;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import com.cvesters.notula.common.domain.Origin;
import com.cvesters.notula.textblock.bdo.TextBlockAction;
import com.cvesters.notula.textblock.dto.TextBlockActionDto;

@Controller
public class TextBlockWebSocket {

	private static final String ENDPOINT = "/text-blocks";

	private TextBlockService textBlockService;

	public TextBlockWebSocket(final TextBlockService textBlockService) {
		this.textBlockService = textBlockService;
	}

	@MessageMapping(ENDPOINT + "/update")
	public void update(final Origin origin,
			@Valid @Payload final TextBlockActionDto.Update dto) {
		final long blockId = dto.getBlockId();
		final TextBlockAction.Update action = dto.toBdo();
		textBlockService.update(origin, blockId, action);
	}
}
