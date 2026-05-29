package com.cvesters.notula.textblock.dto;

import java.util.Objects;

import lombok.Getter;

import com.cvesters.notula.textblock.dao.TextBlockEvent;

@Getter
public class TextBlockEventDto {

	private final long topicId;
	private final long blockId;
	private final TextBlockMutationDto mutation;

	public TextBlockEventDto(final TextBlockEvent event) {
		Objects.requireNonNull(event);

		this.topicId = event.topicId();
		this.blockId = event.blockId();
		this.mutation = TextBlockMutationDto.of(event.action());
	}

	public String getTarget() {
		return "TEXT_BLOCK";
	}
}
