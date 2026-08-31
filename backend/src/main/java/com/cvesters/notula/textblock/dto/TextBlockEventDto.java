package com.cvesters.notula.textblock.dto;

import java.util.Objects;

import lombok.Getter;

import com.cvesters.notula.common.dto.OriginDto;
import com.cvesters.notula.textblock.dao.TextBlockEvent;

@Getter
public class TextBlockEventDto {

	private final long blockId;
	private final TextBlockMutationDto mutation;
	private final OriginDto origin;

	public TextBlockEventDto(final TextBlockEvent event) {
		Objects.requireNonNull(event);

		this.blockId = event.block().getId();
		this.mutation = TextBlockMutationDto.of(event.action());
		this.origin = new OriginDto(event.origin());
	}

	public String getTarget() {
		return "TEXT_BLOCK";
	}
}
