package com.cvesters.notula.meeting;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import com.cvesters.notula.TestContainerConfig;
import com.cvesters.notula.meeting.dao.MeetingDao;
import com.cvesters.notula.organisation.TestOrganisation;

@Sql({ "/db/organisations.sql", "/db/meetings.sql" })
@DataJpaTest
@Import(TestContainerConfig.class)
@AutoConfigureTestDatabase(replace = Replace.NONE)
class MeetingRepositoryTest {

	@Autowired
	private MeetingRepository meetingRepository;

	@PersistenceContext
	private EntityManager entityManager;

	@Nested
	class FindAllByOrganisationId {

		@Test
		void single() {
			final long organisationId = TestOrganisation.GLOVER.getId();

			final List<MeetingDao> result = meetingRepository
					.findAllByOrganisationId(organisationId);

			assertThat(result).hasSize(1).anySatisfy(meeting -> {
				final TestMeeting expectedMeeting = TestMeeting.GLOVER_KICKOFF_2026;
				assertThat(meeting.getId()).isEqualTo(expectedMeeting.getId());
				assertThat(meeting.getOrganisationId())
						.isEqualTo(expectedMeeting.getOrganisation().getId());
				assertThat(meeting.getName())
						.isEqualTo(expectedMeeting.getName());
			});
		}

		@Test
		void multiple() {
			final long organisationId = TestOrganisation.SPORER.getId();

			final List<MeetingDao> result = meetingRepository
					.findAllByOrganisationId(organisationId);

			assertThat(result).hasSize(3).anySatisfy(meeting -> {
				final TestMeeting expectedMeeting = TestMeeting.SPORER_PROJECT;
				assertThat(meeting.getId()).isEqualTo(expectedMeeting.getId());
				assertThat(meeting.getOrganisationId())
						.isEqualTo(expectedMeeting.getOrganisation().getId());
				assertThat(meeting.getName())
						.isEqualTo(expectedMeeting.getName());
			}).anySatisfy(meeting -> {
				final TestMeeting expectedMeeting = TestMeeting.SPORER_RETRO;
				assertThat(meeting.getId()).isEqualTo(expectedMeeting.getId());
				assertThat(meeting.getOrganisationId())
						.isEqualTo(expectedMeeting.getOrganisation().getId());
				assertThat(meeting.getName())
						.isEqualTo(expectedMeeting.getName());
			}).anySatisfy(meeting -> {
				final TestMeeting expectedMeeting = TestMeeting.SPORER_Q2_PLANNING;
				assertThat(meeting.getId()).isEqualTo(expectedMeeting.getId());
				assertThat(meeting.getOrganisationId())
						.isEqualTo(expectedMeeting.getOrganisation().getId());
				assertThat(meeting.getName())
						.isEqualTo(expectedMeeting.getName());
			});
		}

		@Test
		void notFound() {
			final List<MeetingDao> result = meetingRepository
					.findAllByOrganisationId(Long.MAX_VALUE);

			assertThat(result).isEmpty();
		}
	}

}
