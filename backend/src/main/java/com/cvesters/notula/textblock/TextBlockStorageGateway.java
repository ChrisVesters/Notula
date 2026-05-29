package com.cvesters.notula.textblock;

import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.cvesters.notula.textblock.bdo.TextBlockInfo;
import com.cvesters.notula.textblock.dao.TextBlockDao;

@Service
public class TextBlockStorageGateway {

	private final TextBlockRepository textBlockRepository;

	public TextBlockStorageGateway(
			final TextBlockRepository textBlockRepository) {
		this.textBlockRepository = textBlockRepository;
	}

	public Optional<TextBlockInfo> find(final long blockId) {
		return textBlockRepository.findByBlockId(blockId)
				.map(TextBlockDao::toBdo);
	}

	public TextBlockInfo update(final TextBlockInfo bdo) {
		Objects.requireNonNull(bdo);

		final TextBlockDao dao = textBlockRepository
				.findByBlockId(bdo.getBlockId())
				.orElseGet(() -> new TextBlockDao(bdo));
		dao.update(bdo);
		final TextBlockDao saved = textBlockRepository.save(dao);
		return saved.toBdo();
	}
}
