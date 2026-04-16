package com.cvesters.notula.meeting;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.cvesters.notula.common.exception.MissingEntityException;
import com.cvesters.notula.meeting.bdo.MeetingInfo;
import com.cvesters.notula.meeting.dao.MeetingDao;

@Service
public class MeetingStorageGateway {

	private final MeetingRepository meetingRepository;

	public MeetingStorageGateway(final MeetingRepository meetingRepository) {
		this.meetingRepository = meetingRepository;
	}

	public Optional<MeetingInfo> findByOrganisationIdAndId(
			final long organisationid, final long id) {
		return meetingRepository.findByOrganisationIdAndId(organisationid, id)
				.map(MeetingDao::toBdo);
	}

	public List<MeetingInfo> findAllByOrganisationId(
			final long organisationId) {
		return meetingRepository.findAllByOrganisationId(organisationId)
				.stream()
				.map(MeetingDao::toBdo)
				.toList();
	}

	public MeetingInfo create(final MeetingInfo meeting) {
		Objects.requireNonNull(meeting);

		final var dao = new MeetingDao(meeting);
		final var saved = meetingRepository.save(dao);
		return saved.toBdo();
	}

	public MeetingInfo update(final MeetingInfo meeting) {
		Objects.requireNonNull(meeting);

		final var dao = meetingRepository
				.findByOrganisationIdAndId(meeting.getOrganisationId(),
						meeting.getId())
				.orElseThrow(MissingEntityException::new);
		dao.update(meeting);
		final var saved = meetingRepository.save(dao);
		return saved.toBdo();
	}
}
