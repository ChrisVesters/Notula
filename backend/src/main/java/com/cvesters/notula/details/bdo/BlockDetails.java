package com.cvesters.notula.details.bdo;

import java.util.Objects;

import lombok.Getter;

import com.cvesters.notula.common.domain.Rank;
import com.cvesters.notula.block.bdo.BlockInfo;

@Getter
public class BlockDetails {

	private final long id;
	private final Rank rank;
	private final BlockContent content;

	public BlockDetails(final BlockInfo info, final BlockContent content) {
		Objects.requireNonNull(info);
		Objects.requireNonNull(content);

		this.id = info.getId();
		this.rank = info.getRank();
		this.content = content;
	}
}
