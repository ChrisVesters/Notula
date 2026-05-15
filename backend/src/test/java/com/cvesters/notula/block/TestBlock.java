package com.cvesters.notula.block;

import java.util.Arrays;
import java.util.List;

import lombok.Getter;

import com.cvesters.notula.block.bdo.BlockInfo;
import com.cvesters.notula.block.bdo.BlockType;
import com.cvesters.notula.topic.TestTopic;

@Getter
public enum TestBlock {
	SPORER_PROJECT_DELIVERABLES_FIRST(1L, TestTopic.SPORER_PROJECT_DELIVERABLES,
			BlockType.TEXT, 0),
	SPORER_PROJECT_BLOCKERS_FIRST(2L, TestTopic.SPORER_PROJECT_BLOCKERS,
			BlockType.TEXT, 0),
	SPORER_PROJECT_BLOCKERS_SECOND(3L, TestTopic.SPORER_PROJECT_BLOCKERS,
			BlockType.TEXT, 1),
	SPORER_PROJECT_BLOCKERS_THIRD(4L, TestTopic.SPORER_PROJECT_BLOCKERS,
			BlockType.TEXT, 2);

	private final long id;
	private final TestTopic topic;
	private final BlockType type;
	private final int sequenceId;

	TestBlock(final long id, final TestTopic topic, final BlockType type,
			final int sequenceId) {
		this.id = id;
		this.topic = topic;
		this.type = type;
		this.sequenceId = sequenceId;
	}

	public static List<TestBlock> ofTopic(final TestTopic topic) {
		return Arrays.stream(TestBlock.values())
				.filter(block -> block.topic.equals(topic))
				.toList();
	}

	public BlockInfo info() {
		return new BlockInfo(id, topic.getOrganisation().getId(), topic.getId(),
				type, sequenceId);
	}

	public int getTypeId() {
		return switch (type) {
			case TEXT -> 0;
		};
	}

	public String getTypeString() {
		return switch (type) {
			case TEXT -> "TEXT";
		};
	}
}
