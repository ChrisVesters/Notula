package com.cvesters.notula.block.dto;

import java.util.Objects;

import lombok.Getter;

import com.cvesters.notula.block.bdo.BlockEvent;
import com.cvesters.notula.common.dto.OriginDto;

@Getter
public class BlockEventDto {
	
	private final long blockId;
	private final BlockMutationDto mutation;
	private final OriginDto origin;

	public BlockEventDto(final BlockEvent event) {
		Objects.requireNonNull(event);

		this.blockId = event.block().getId();
		this.mutation = BlockMutationDto.of(event.action());
		this.origin = new OriginDto(event.origin());
	}

	public String getTarget() {
		return "BLOCK";
	}
}
