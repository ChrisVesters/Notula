package com.cvesters.notula.meeting;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import com.cvesters.notula.meeting.dao.MeetingDao;

public interface MeetingRepository extends Repository<MeetingDao, Long> {

	Optional<MeetingDao> findById(long id);

	List<MeetingDao> findAllByOrganisationId(long organisationId);

	MeetingDao save(MeetingDao meeting);

}