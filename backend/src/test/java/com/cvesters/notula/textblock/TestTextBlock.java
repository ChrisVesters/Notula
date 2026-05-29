package com.cvesters.notula.textblock;

import java.util.Arrays;

import lombok.Getter;

import com.cvesters.notula.block.TestBlock;
import com.cvesters.notula.textblock.bdo.TextBlockInfo;

@Getter
public enum TestTextBlock {
	SPORER_PROJECT_BLOCKERS_FIRST(TestBlock.SPORER_PROJECT_BLOCKERS_FIRST,
			"start"),
	SPORER_PROJECT_BLOCKERS_SECOND(TestBlock.SPORER_PROJECT_BLOCKERS_SECOND,
			"We do not have the time to do this");

	private final TestBlock block;
	private final String content;

	TestTextBlock(final TestBlock block, final String content) {
		this.block = block;
		this.content = content;
	}

	public static TestTextBlock ofBlock(final TestBlock block) {
		return Arrays.stream(TestTextBlock.values())
				.filter(textBlock -> textBlock.block == block)
				.findFirst()
				.orElseThrow();
	}

	public TextBlockInfo info() {
		return new TextBlockInfo(block.getId(), content);
	}
}
