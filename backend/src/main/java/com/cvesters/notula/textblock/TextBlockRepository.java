package com.cvesters.notula.textblock;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import com.cvesters.notula.textblock.dao.TextBlockDao;

public interface TextBlockRepository extends Repository<TextBlockDao, Long> {

	Optional<TextBlockDao> findByBlockId(long blockId);

	TextBlockDao save(TextBlockDao textBlockDao);

}
