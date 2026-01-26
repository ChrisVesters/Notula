package com.cvesters.notula.meeting;

import java.util.List;

import org.springframework.data.repository.Repository;

import com.cvesters.notula.meeting.dao.MeetingDao;

public interface MeetingRepository extends Repository<MeetingDao, Long> {

	List<MeetingDao> findAllByOrganisationId(long organisationId);

}