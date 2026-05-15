package com.cvesters.notula.block;

import jakarta.validation.Valid;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import com.cvesters.notula.block.bdo.BlockAction;
import com.cvesters.notula.block.dto.BlockActionDto;
import com.cvesters.notula.common.controller.BaseController;
import com.cvesters.notula.common.domain.Principal;

@Controller
public class BlockWebSocket extends BaseController {

	private static final String BASE_ENDPOINT = "/meetings/{meetingId}/topics/{topicId}/blocks";

	private BlockService blockService;

	public BlockWebSocket(final BlockService blockService) {
		this.blockService = blockService;
	}

	@MessageMapping(BASE_ENDPOINT)
	public void create(@DestinationVariable final long meetingId,
			@DestinationVariable final long topicId,
			@Valid @Payload final BlockActionDto.Create dto) {
		final Principal principal = getPrincipal();

		final BlockAction.Create action = dto.toBdo();
		blockService.create(principal, meetingId, topicId, action);
	}
}
