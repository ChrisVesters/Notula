package com.cvesters.notula.block;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import com.cvesters.notula.block.dao.BlockDao;

public interface BlockRepository extends Repository<BlockDao, Long> {

	List<BlockDao> findAllByTopicId(long topicId);

	Optional<BlockDao> findByTopicIdAndId(long topicId, long id);

	BlockDao save(BlockDao block);

	List<BlockDao> saveAll(Iterable<BlockDao> blocks);

}