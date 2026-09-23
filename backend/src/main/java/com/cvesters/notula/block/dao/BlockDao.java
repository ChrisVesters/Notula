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
import com.cvesters.notula.common.domain.Rank;

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
	private BlockType type;

	@Column(nullable = false)
	private String rank;

	public BlockDao(final BlockInfo bdo) {
		Objects.requireNonNull(bdo);

		this.organisationId = bdo.getOrganisationId();
		this.topicId = bdo.getTopicId();
		this.type = bdo.getType();
		this.rank = bdo.getRank().value();
	}

	public void update(final BlockInfo bdo) {
		Objects.requireNonNull(bdo);

		this.rank = bdo.getRank().value();
	}

	public BlockInfo toBdo() {
		Validate.validState(id != null);

		return new BlockInfo(id, organisationId, topicId, type,
				new Rank(rank));
	}
}
