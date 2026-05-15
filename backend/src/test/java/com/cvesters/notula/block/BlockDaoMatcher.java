package com.cvesters.notula.block;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;

import org.assertj.core.api.Condition;

import com.cvesters.notula.block.dao.BlockDao;

public class BlockDaoMatcher {

	private final EntityManager entityManager;
	private final BlockDao expected;

	public BlockDaoMatcher(final EntityManager entityManager,
			final BlockDao expected) {
		this.entityManager = entityManager;
		this.expected = expected;
	}

	public Condition<BlockDao> created() {
		return new Condition<>(saved -> {
			assertThat(saved.getId()).isNotNull();
			assertThat(saved.getOrganisationId())
					.isEqualTo(expected.getOrganisationId());
			assertThat(saved.getTopicId()).isEqualTo(expected.getTopicId());
			assertThat(saved.getType()).isEqualTo(expected.getType());
			assertThat(saved.getSequenceId())
					.isEqualTo(expected.getSequenceId());
			return true;
		}, "created");
	}

	public Condition<BlockDao> found() {
		return new Condition<>(saved -> {
			final BlockDao found = entityManager.find(saved.getClass(),
					saved.getId());

			assertThat(found).isNotNull();
			assertThat(found.getId()).isEqualTo(saved.getId());
			assertThat(found.getOrganisationId())
					.isEqualTo(saved.getOrganisationId());
			assertThat(found.getTopicId()).isEqualTo(saved.getTopicId());
			assertThat(found.getType()).isEqualTo(saved.getType());
			assertThat(found.getSequenceId()).isEqualTo(saved.getSequenceId());
			return true;
		}, "found");

	}

}
