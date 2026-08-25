package com.cvesters.notula.block;

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

	public Optional<BlockInfo> find(final long id) {
		return blockRepository.findById(id).map(BlockDao::toBdo);
	}

	public List<BlockInfo> findAllByTopicId(final long topicId) {
		return blockRepository.findAllByTopicId(topicId)
				.stream()
				.map(BlockDao::toBdo)
				.toList();
	}

	public BlockInfo update(final BlockInfo block) {
		Objects.requireNonNull(block);

		final BlockDao dao = blockRepository.findById(block.getId())
				.orElseThrow(MissingEntityException::new);
		dao.update(block);
		final BlockDao saved = blockRepository.save(dao);
		return saved.toBdo();
	}

	public void delete(final BlockInfo block) {
		Objects.requireNonNull(block);

		final BlockDao dao = blockRepository.findById(block.getId())
				.orElseThrow(MissingEntityException::new);

		blockRepository.delete(dao);
	}
}
