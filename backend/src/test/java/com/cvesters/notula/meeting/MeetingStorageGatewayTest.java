package com.cvesters.notula.meeting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.meeting.bdo.MeetingInfo;
import com.cvesters.notula.meeting.dao.MeetingDao;
import com.cvesters.notula.organisation.TestOrganisation;

class MeetingStorageGatewayTest {

	private final MeetingRepository meetingRepository = mock();

	private final MeetingStorageGateway gateway = new MeetingStorageGateway(
			meetingRepository);

	@Nested
	class FindAllByOrganisationId {

		@Test
		void single() {
			final long organisationId = TestOrganisation.GLOVER.getId();
			final List<TestMeeting> found = List
					.of(TestMeeting.GLOVER_KICKOFF_2026);

			final var daos = new ArrayList<MeetingDao>();
			final var bdos = new ArrayList<MeetingInfo>();
			for (final TestMeeting meeting : found) {
				final MeetingDao dao = mock();
				final MeetingInfo bdo = meeting.info();
				when(dao.toBdo()).thenReturn(bdo);

				daos.add(dao);
				bdos.add(bdo);
			}

			when(meetingRepository.findAllByOrganisationId(organisationId))
					.thenReturn(daos);

			final List<MeetingInfo> result = gateway
					.findAllByOrganisationId(organisationId);

			assertThat(result).isEqualTo(bdos);
		}

		@Test
		void multiple() {
			final long organisationId = TestOrganisation.SPORER.getId();
			final List<TestMeeting> found = List.of(TestMeeting.SPORER_PROJECT,
					TestMeeting.SPORER_Q2_PLANNING, TestMeeting.SPORER_RETRO);

			final var daos = new ArrayList<MeetingDao>();
			final var bdos = new ArrayList<MeetingInfo>();
			for (final TestMeeting meeting : found) {
				final MeetingDao dao = mock();
				final MeetingInfo bdo = meeting.info();
				when(dao.toBdo()).thenReturn(bdo);

				daos.add(dao);
				bdos.add(bdo);
			}

			when(meetingRepository.findAllByOrganisationId(organisationId))
					.thenReturn(daos);

			final List<MeetingInfo> result = gateway
					.findAllByOrganisationId(organisationId);

			assertThat(result).isEqualTo(bdos);
		}

		@Test
		void notFound() {
			final long organisationId = Long.MAX_VALUE;

			when(meetingRepository.findAllByOrganisationId(organisationId))
					.thenReturn(Collections.emptyList());

			final List<MeetingInfo> result = gateway
					.findAllByOrganisationId(organisationId);

			assertThat(result).isEmpty();
		}
	}
}
