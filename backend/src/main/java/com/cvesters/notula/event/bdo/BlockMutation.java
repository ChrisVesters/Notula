package com.cvesters.notula.event.bdo;

import java.util.Objects;

import com.cvesters.notula.block.bdo.BlockType;
import com.cvesters.notula.common.domain.Rank;

public sealed interface BlockMutation extends Mutation {

	record Add(long blockId, long topicId, BlockType type, Rank rank)
			implements BlockMutation {

		public Add {
			Objects.requireNonNull(type);
			Objects.requireNonNull(rank);
		}
	}

	record Move(long blockId, Rank rank) implements BlockMutation {

		public Move {
			Objects.requireNonNull(rank);
		}
	}

	record Remove(long blockId) implements BlockMutation {
	}
}
