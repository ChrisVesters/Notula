package com.cvesters.notula.topic;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import com.cvesters.notula.topic.dao.TopicDao;

public interface TopicRepository extends Repository<TopicDao, Long> {

	List<TopicDao> findAllByMeetingId(long meetingId);

	Optional<TopicDao> findByMeetingIdAndId(long meetingId, long id);

	TopicDao save(TopicDao topic);

}