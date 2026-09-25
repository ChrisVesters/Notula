package com.cvesters.notula.event.bdo;

import java.util.Objects;

import org.apache.commons.lang3.Validate;

import com.cvesters.notula.common.domain.Minutes;
import com.cvesters.notula.common.domain.Rank;

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

	record Rename(long topicId, int position, int length, String value)
			implements TopicMutation {

		public Rename {
			Objects.requireNonNull(value);
			Validate.isTrue(position >= 0);
			Validate.isTrue(length >= 0);
		}
	}

	record Describe(long topicId, int position, int length, String value)
			implements TopicMutation {

		public Describe {
			Objects.requireNonNull(value);
			Validate.isTrue(position >= 0);
			Validate.isTrue(length >= 0);
		}
	}

	record Schedule(long topicId, Minutes duration) implements TopicMutation {
	}

	record Remove(long topicId) implements TopicMutation {
	}
}
