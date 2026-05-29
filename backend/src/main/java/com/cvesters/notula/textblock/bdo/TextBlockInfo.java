package com.cvesters.notula.textblock.bdo;

import java.util.Objects;

import lombok.Getter;

@Getter
public class TextBlockInfo {

	private final long blockId;
	private String content;

	public TextBlockInfo(final long blockId, final String content) {
		Objects.requireNonNull(content);

		this.blockId = blockId;
		this.content = content;
	}

	public void setContent(final String content) {
		Objects.requireNonNull(content);
		
		this.content = content;
	}
}
