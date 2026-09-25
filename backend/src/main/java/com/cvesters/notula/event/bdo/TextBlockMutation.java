package com.cvesters.notula.event.bdo;

import java.util.Objects;

import org.apache.commons.lang3.Validate;

public sealed interface TextBlockMutation extends Mutation {

	record Edit(long blockId, int position, int length, String value)
			implements TextBlockMutation {

		public Edit {
			Objects.requireNonNull(value);
			Validate.isTrue(position >= 0);
			Validate.isTrue(length >= 0);
		}
	}
}
