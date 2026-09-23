package com.cvesters.notula.details.dto;

import java.util.Objects;

import lombok.Getter;

import com.cvesters.notula.details.bdo.BlockDetails;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

@Getter
public class BlockDetailsDto {

	private final long id;
	private final String rank;

	@JsonUnwrapped
	private final BlockContentDto content;

	protected BlockDetailsDto(final BlockDetails details) {
		Objects.requireNonNull(details);

		this.id = details.getId();
		this.rank = details.getRank().value();
		this.content = BlockContentDto.of(details.getContent());
	}
}
