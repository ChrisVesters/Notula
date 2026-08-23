package com.cvesters.notula.block;

import jakarta.validation.Valid;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import com.cvesters.notula.block.bdo.BlockAction;
import com.cvesters.notula.block.dto.BlockActionDto;
import com.cvesters.notula.common.controller.BaseController;
import com.cvesters.notula.common.domain.Principal;

@Controller
public class BlockWebSocket extends BaseController {

	private static final String ENDPOINT = "/blocks";

	private BlockService blockService;

	public BlockWebSocket(final BlockService blockService) {
		this.blockService = blockService;
	}

	@MessageMapping(ENDPOINT + "/create")
	public void create(@Valid @Payload final BlockActionDto.Create dto) {
		final Principal principal = getPrincipal();

		final BlockAction.Create action = dto.toBdo();
		blockService.create(principal, action);
	}

	@MessageMapping(ENDPOINT + "/move")
	public void move(@Valid @Payload final BlockActionDto.Move dto) {
		final Principal principal = getPrincipal();

		final long blockId = dto.getBlockId();
		final BlockAction.Move action = dto.toBdo();
		blockService.move(principal, blockId, action);
	}

	@MessageMapping(ENDPOINT + "/delete")
	public void delete(@Valid @Payload final BlockActionDto.Delete dto) {
		final Principal principal = getPrincipal();

		final long blockId = dto.getBlockId();
		blockService.delete(principal, blockId);
	}
}
