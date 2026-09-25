package com.cvesters.notula.event.bdo;

import java.util.Objects;

import com.cvesters.notula.common.domain.Splice;

public sealed interface TextBlockMutation extends Mutation {

	record Edit(long blockId, Splice edit) implements TextBlockMutation {

		public Edit {
			Objects.requireNonNull(edit);
		}
	}
}
