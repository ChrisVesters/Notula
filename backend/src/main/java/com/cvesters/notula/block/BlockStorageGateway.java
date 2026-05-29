package com.cvesters.notula.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.cvesters.notula.block.bdo.BlockInfo;
import com.cvesters.notula.block.dao.BlockDao;
import com.cvesters.notula.common.exception.MissingEntityException;

@Service
public class BlockStorageGateway {

	private final BlockRepository blockRepository;

	public BlockStorageGateway(final BlockRepository blockRepository) {
		this.blockRepository = blockRepository;
	}

	public BlockInfo create(final BlockInfo block) {
		Objects.requireNonNull(block);

		final var dao = new BlockDao(block);
		final var saved = blockRepository.save(dao);
		return saved.toBdo();
	}

	public Optional<BlockInfo> find(final long topicId, final long id) {
		return blockRepository.findByTopicIdAndId(topicId, id)
				.map(BlockDao::toBdo);
	}

	public List<BlockInfo> findAllByTopicId(final long topicId) {
		return blockRepository.findAllByTopicId(topicId)
				.stream()
				.map(BlockDao::toBdo)
				.toList();
	}

	public List<BlockInfo> updateAll(final List<BlockInfo> blocks) {
		Objects.requireNonNull(blocks);

		final var updated = new ArrayList<BlockDao>();
		for (final BlockInfo block : blocks) {
			final BlockDao dao = blockRepository
					.findByTopicIdAndId(block.getTopicId(), block.getId())
					.orElseThrow(MissingEntityException::new);
			dao.update(block);
			updated.add(dao);
		}

		return blockRepository.saveAll(updated)
				.stream()
				.map(BlockDao::toBdo)
				.toList();
	}
}
