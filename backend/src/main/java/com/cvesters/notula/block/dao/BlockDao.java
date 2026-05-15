package com.cvesters.notula.block.dao;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import org.apache.commons.lang3.Validate;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.cvesters.notula.block.bdo.BlockInfo;
import com.cvesters.notula.block.bdo.BlockType;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Entity(name = "blocks")
public class BlockDao {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "organisation_id", nullable = false, updatable = false)
	private long organisationId;

	@Column(name = "topic_id", nullable = false, updatable = false)
	private long topicId;

	@Column(name = "type", nullable = false, updatable = false)
	private int type;

	@Column(name = "sequence_id", nullable = false)
	private int sequenceId;

	public BlockDao(final BlockInfo bdo) {
		Objects.requireNonNull(bdo);

		this.organisationId = bdo.getOrganisationId();
		this.topicId = bdo.getTopicId();
		this.type = BlockTypeDao.toDao(bdo.getType());
		this.sequenceId = bdo.getSequenceId();
	}

	public void update(final BlockInfo bdo) {
		Objects.requireNonNull(bdo);

		this.sequenceId = bdo.getSequenceId();
	}

	public BlockInfo toBdo() {
		Validate.validState(id != null);

		final BlockType blockType = BlockTypeDao.toBdo(type);
		return new BlockInfo(id, organisationId, topicId, blockType,
				sequenceId);
	}
}
