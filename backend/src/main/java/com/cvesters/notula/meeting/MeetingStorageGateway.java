package com.cvesters.notula.meeting;

import java.util.List;

import org.springframework.stereotype.Service;

import com.cvesters.notula.meeting.bdo.MeetingInfo;
import com.cvesters.notula.meeting.dao.MeetingDao;

@Service
public class MeetingStorageGateway {

	private final MeetingRepository meetingRepository;

	public MeetingStorageGateway(final MeetingRepository meetingRepository) {
		this.meetingRepository = meetingRepository;
	}

	public List<MeetingInfo> findAllByOrganisationId(
			final long organisationId) {
		return meetingRepository
				.findAllByOrganisationId(organisationId)
				.stream().map(MeetingDao::toBdo).toList();
	}
}
