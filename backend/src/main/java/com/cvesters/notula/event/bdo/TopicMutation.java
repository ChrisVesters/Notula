package com.cvesters.notula.event.bdo;

import java.util.Objects;

import com.cvesters.notula.common.domain.Minutes;
import com.cvesters.notula.common.domain.Rank;
import com.cvesters.notula.common.domain.Splice;

public sealed interface TopicMutation extends Mutation {

	record Add(long topicId, Rank rank, String name) implements TopicMutation {

		public Add {
			Objects.requireNonNull(rank);
			Objects.requireNonNull(name);
		}
	}

	record Move(long topicId, Rank rank) implements TopicMutation {

		public Move {
			Objects.requireNonNull(rank);
		}
	}

	record Rename(long topicId, Splice edit) implements TopicMutation {

		public Rename {
			Objects.requireNonNull(edit);
		}
	}

	record Describe(long topicId, Splice edit) implements TopicMutation {

		public Describe {
			Objects.requireNonNull(edit);
		}
	}

	record Schedule(long topicId, Minutes duration) implements TopicMutation {
	}

	record Remove(long topicId) implements TopicMutation {
	}
}
