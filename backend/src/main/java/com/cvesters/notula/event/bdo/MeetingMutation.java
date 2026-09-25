package com.cvesters.notula.event.bdo;

import java.util.Objects;

import org.apache.commons.lang3.Validate;

public sealed interface MeetingMutation extends Mutation {

	record Add(String name) implements MeetingMutation {

		public Add {
			Objects.requireNonNull(name);
		}
	}

	record Rename(int position, int length, String value)
			implements MeetingMutation {

		public Rename {
			Objects.requireNonNull(value);
			Validate.isTrue(position >= 0);
			Validate.isTrue(length >= 0);
		}
	}

	record Describe(int position, int length, String value)
			implements MeetingMutation {

		public Describe {
			Objects.requireNonNull(value);
			Validate.isTrue(position >= 0);
			Validate.isTrue(length >= 0);
		}
	}

	record Remove() implements MeetingMutation {
	}
}
