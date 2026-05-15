package com.cvesters.notula.details.bdo;

import java.util.List;
import java.util.Objects;

import lombok.Getter;

import com.cvesters.notula.topic.bdo.TopicInfo;

@Getter
public class TopicDetails {

	private final long id;
	private final String name;

	private List<BlockDetails> blocks;

	public TopicDetails(final TopicInfo info, final List<BlockDetails> blocks) {
		Objects.requireNonNull(info);
		Objects.requireNonNull(blocks);

		this.id = info.getId();
		this.name = info.getName();

		this.blocks = List.copyOf(blocks);
	}

}
