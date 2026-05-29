package com.cvesters.notula.details.dto;

import java.util.Objects;

import lombok.Getter;

import com.cvesters.notula.block.bdo.BlockType;
import com.cvesters.notula.block.dto.BlockTypeDto;
import com.cvesters.notula.details.bdo.BlockContent;

// TODO: Can this be improved with generics?
@Getter
public class BlockContentDto {

	private final String type;

	protected BlockContentDto(final BlockType type) {
		this.type = BlockTypeDto.toDto(type);
	}

	public static BlockContentDto of(final BlockContent content) {
		Objects.requireNonNull(content);

		return switch (content) {
			case BlockContent.Text text -> new Text(text);
		};
	}

	@Getter
	public static final class Text extends BlockContentDto {

		private final String content;

		private Text(final BlockContent.Text textContent) {
			super(BlockType.TEXT);

			this.content = textContent.content();
		}
	}
}
