package com.cvesters.notula.topic;

import java.util.List;

import org.springframework.data.repository.Repository;

import com.cvesters.notula.topic.dao.TopicDao;

public interface TopicRepository extends Repository<TopicDao, Long> {
	
	List<TopicDao> findAllByMeetingId(long meetingId);

	TopicDao save(TopicDao topic);
}
