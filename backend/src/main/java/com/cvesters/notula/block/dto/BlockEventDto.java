package com.cvesters.notula.block.dto;

import java.util.Objects;

import lombok.Getter;

import com.cvesters.notula.block.bdo.BlockEvent;

@Getter
public class BlockEventDto {
	
	private final long blockId;
	private final BlockMutationDto mutation;

	public BlockEventDto(final BlockEvent event) {
		Objects.requireNonNull(event);

		this.blockId = event.blockId();
		this.mutation = BlockMutationDto.of(event.action());
	}

	public String getTarget() {
		return "BLOCK";
	}
}
