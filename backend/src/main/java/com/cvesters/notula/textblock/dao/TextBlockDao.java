package com.cvesters.notula.textblock.dao;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.cvesters.notula.textblock.bdo.TextBlockInfo;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Entity(name = "text_blocks")
public class TextBlockDao {

	@Id
	@Column(name = "block_id", nullable = false, updatable = false)
	private long blockId;

	@Column(nullable = false)
	private String content;

	public TextBlockDao(final TextBlockInfo bdo) {
		Objects.requireNonNull(bdo);

		this.blockId = bdo.getBlockId();
		this.content = bdo.getContent();
	}

	public void update(final TextBlockInfo bdo) {
		Objects.requireNonNull(bdo);

		this.content = bdo.getContent();
	}

	public TextBlockInfo toBdo() {
		return new TextBlockInfo(blockId, content);
	}
}
