package com.cvesters.notula.meeting.dto;

import java.util.Objects;

import com.cvesters.notula.block.bdo.BlockAction;
import com.cvesters.notula.block.bdo.BlockEvent;
import com.cvesters.notula.block.dto.BlockTypeDto;

public sealed interface BlockMutationDto extends MutationDto {

	static BlockMutationDto of(final BlockEvent event) {
		Objects.requireNonNull(event);

		final long block = event.block().getId();

		return switch (event.action()) {
			case BlockAction.Create action -> new Add(block,
					action.getTopicId(), new BlockTypeDto(action.getType()),
					action.getSequenceId());
			case BlockAction.Move action -> new Move(block,
					action.getSequenceId());
			case BlockAction.Delete _ -> new Remove(block);
		};
	}

	record Add(long block, long topic, BlockTypeDto blockType, int sequenceId)
			implements BlockMutationDto {
	}

	record Move(long block, int sequenceId) implements BlockMutationDto {
	}

	record Remove(long block) implements BlockMutationDto {
	}
}
