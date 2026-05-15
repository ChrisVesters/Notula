package com.cvesters.notula.details.dto;

import java.util.List;
import java.util.Objects;

import lombok.Getter;

import com.cvesters.notula.details.bdo.TopicDetails;

@Getter
public class TopicDetailsDto {

	private final long id;
	private final String name;

	private final List<BlockDetailsDto> blocks;

	public TopicDetailsDto(final TopicDetails details) {
		Objects.requireNonNull(details);

		this.id = details.getId();
		this.name = details.getName();
		this.blocks = details.getBlocks()
				.stream()
				.map(BlockDetailsDto::new)
				.toList();
	}

}
