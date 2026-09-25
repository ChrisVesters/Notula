package com.cvesters.notula.textblock.bdo;

import lombok.Getter;

import com.cvesters.notula.common.domain.Splice;
import com.cvesters.notula.common.domain.TextUpdate;

public sealed interface TextBlockAction {

	sealed interface Update extends TextBlockAction {

		void apply(final TextBlockInfo object);
	}

	@Getter
	final class UpdateContent extends TextUpdate<TextBlockInfo>
			implements TextBlockAction.Update {

		public UpdateContent(final Splice edit) {
			super(TextBlockInfo::getContent, TextBlockInfo::setContent, edit);
		}
	}
}
