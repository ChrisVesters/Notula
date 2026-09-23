package com.cvesters.notula.topic;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;

import org.assertj.core.api.Condition;

import com.cvesters.notula.topic.dao.TopicDao;

public class TopicDaoMatcher {

	private final EntityManager entityManager;
	private final TopicDao expected;

	public TopicDaoMatcher(final EntityManager entityManager,
			final TopicDao expected) {
		this.entityManager = entityManager;
		this.expected = expected;
	}

	public Condition<TopicDao> created() {
		return new Condition<>(saved -> {
			assertThat(saved.getId()).isNotNull();
			assertThat(saved.getOrganisationId())
					.isEqualTo(expected.getOrganisationId());
			assertThat(saved.getMeetingId()).isEqualTo(expected.getMeetingId());
			assertThat(saved.getRank()).isEqualTo(expected.getRank());
			assertThat(saved.getName()).isEqualTo(expected.getName());
			assertThat(saved.getDescription())
					.isEqualTo(expected.getDescription());
			assertThat(saved.getDuration()).isEqualTo(expected.getDuration());
			return true;
		}, "created");
	}

	public Condition<TopicDao> found() {
		return new Condition<>(saved -> {
			final TopicDao found = entityManager.find(saved.getClass(),
					saved.getId());

			assertThat(found).isNotNull();
			assertThat(found.getId()).isEqualTo(saved.getId());
			assertThat(found.getOrganisationId())
					.isEqualTo(saved.getOrganisationId());
			assertThat(found.getMeetingId()).isEqualTo(saved.getMeetingId());
			assertThat(found.getRank()).isEqualTo(saved.getRank());
			assertThat(found.getName()).isEqualTo(saved.getName());
			assertThat(found.getDescription())
					.isEqualTo(saved.getDescription());
			assertThat(found.getDuration()).isEqualTo(saved.getDuration());
			return true;
		}, "found");

	}
}
