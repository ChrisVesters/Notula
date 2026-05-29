package com.cvesters.notula.details.bdo;

import java.util.Objects;

import com.cvesters.notula.textblock.bdo.TextBlockInfo;

public sealed interface BlockContent {

	public static record Text(String content) implements BlockContent {

		public Text {
			Objects.requireNonNull(content);
		}

		public Text() {
			this("");
		}

		public Text(final TextBlockInfo textBlockInfo) {
			Objects.requireNonNull(textBlockInfo);

			this(textBlockInfo.getContent());
		}
	}

}
