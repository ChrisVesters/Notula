package com.cvesters.notula.topic;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.cvesters.notula.common.exception.MissingEntityException;
import com.cvesters.notula.topic.bdo.TopicInfo;
import com.cvesters.notula.topic.dao.TopicDao;

@Service
public class TopicStorageGateway {

	private final TopicRepository topicRepository;

	public TopicStorageGateway(final TopicRepository topicRepository) {
		this.topicRepository = topicRepository;
	}

	public TopicInfo create(final TopicInfo topic) {
		Objects.requireNonNull(topic);

		final var dao = new TopicDao(topic);
		final var saved = topicRepository.save(dao);
		return saved.toBdo();
	}

	public Optional<TopicInfo> find(final long meetingId, final long topicId) {
		return topicRepository.findByMeetingIdAndId(meetingId, topicId)
				.map(TopicDao::toBdo);
	}

	public List<TopicInfo> findAllByMeetingId(final long meetingId) {
		return topicRepository.findAllByMeetingId(meetingId)
				.stream()
				.map(TopicDao::toBdo)
				.toList();
	}

	public TopicInfo update(final TopicInfo topic) {
		Objects.requireNonNull(topic);

		final var dao = topicRepository
				.findByMeetingIdAndId(topic.getMeetingId(), topic.getId())
				.orElseThrow(MissingEntityException::new);
		dao.update(topic);
		final var saved = topicRepository.save(dao);
		return saved.toBdo();
	}

}
