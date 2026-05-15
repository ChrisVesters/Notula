package com.cvesters.notula.details.dto;

import java.util.Objects;

import lombok.Getter;

import com.cvesters.notula.block.dto.BlockTypeDto;
import com.cvesters.notula.details.bdo.BlockDetails;

@Getter
public class BlockDetailsDto {

	private final long id;
	private final String type;
	private final int sequenceId;

	public BlockDetailsDto(final BlockDetails details) {
		Objects.requireNonNull(details);

		this.id = details.getId();
		this.type = BlockTypeDto.toDto(details.getType());
		this.sequenceId = details.getSequenceId();
	}

}
