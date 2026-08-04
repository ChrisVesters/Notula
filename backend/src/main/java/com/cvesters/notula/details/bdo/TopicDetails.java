package com.cvesters.notula.details.bdo;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import lombok.Getter;

import com.cvesters.notula.topic.bdo.TopicInfo;

@Getter
public class TopicDetails {

	private final long id;
	private final int sequenceId;
	private final String name;
	private final String description;
	private final Duration duration;

	private List<BlockDetails> blocks;

	public TopicDetails(final TopicInfo info, final List<BlockDetails> blocks) {
		Objects.requireNonNull(info);
		Objects.requireNonNull(blocks);

		this.id = info.getId();
		this.sequenceId = info.getSequenceId();
		this.name = info.getName();
		this.description = info.getDescription();
		this.duration = info.getDuration().orElse(null);

		this.blocks = List.copyOf(blocks);
	}

	public Optional<Duration> getDuration() {
		return Optional.ofNullable(duration);
	}

}
