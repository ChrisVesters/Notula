package com.cvesters.notula.event.bdo;

import java.util.Objects;

import com.cvesters.notula.common.domain.Splice;

public sealed interface MeetingMutation extends Mutation {

	record Add(String name) implements MeetingMutation {

		public Add {
			Objects.requireNonNull(name);
		}
	}

	record Rename(Splice edit) implements MeetingMutation {

		public Rename {
			Objects.requireNonNull(edit);
		}
	}

	record Describe(Splice edit) implements MeetingMutation {

		public Describe {
			Objects.requireNonNull(edit);
		}
	}

	record Remove() implements MeetingMutation {
	}
}
