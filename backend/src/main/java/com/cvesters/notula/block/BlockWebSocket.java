package com.cvesters.notula.block;

import jakarta.validation.Valid;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import com.cvesters.notula.block.bdo.BlockAction;
import com.cvesters.notula.block.dto.BlockActionDto;
import com.cvesters.notula.common.domain.Origin;

@Controller
public class BlockWebSocket {

	private static final String ENDPOINT = "/blocks";

	private BlockService blockService;

	public BlockWebSocket(final BlockService blockService) {
		this.blockService = blockService;
	}

	@MessageMapping(ENDPOINT + "/create")
	public void create(final Origin origin,
			@Valid @Payload final BlockActionDto.Create dto) {
		final BlockAction.Create action = dto.toBdo();
		blockService.create(origin, action);
	}

	@MessageMapping(ENDPOINT + "/move")
	public void move(final Origin origin,
			@Valid @Payload final BlockActionDto.Move dto) {
		final long blockId = dto.getBlockId();
		final BlockAction.Move action = dto.toBdo();
		blockService.move(origin, blockId, action);
	}

	@MessageMapping(ENDPOINT + "/delete")
	public void delete(final Origin origin,
			@Valid @Payload final BlockActionDto.Delete dto) {
		final long blockId = dto.getBlockId();
		blockService.delete(origin, blockId);
	}
}
