package com.cvesters.notula.meeting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.context.jdbc.Sql;

import com.cvesters.notula.meeting.bdo.MeetingInfo;
import com.cvesters.notula.meeting.dao.MeetingDao;
import com.cvesters.notula.organisation.TestOrganisation;
import com.cvesters.notula.test.RepositoryTest;

@Sql({ "/db/organisations.sql", "/db/meetings.sql" })
class MeetingRepositoryTest extends RepositoryTest {

	@Autowired
	private MeetingRepository meetingRepository;

	@Nested
	class FindByOrganisationIdAndId {

		private static final TestMeeting MEETING = TestMeeting.GLOVER_KICKOFF_2026;

		@Test
		void single() {
			final Optional<MeetingDao> dao = meetingRepository
					.findByOrganisationIdAndId(
							MEETING.getOrganisation().getId(), MEETING.getId());

			assertThat(dao).hasValueSatisfying(meeting -> {
				assertThat(meeting.getId()).isEqualTo(MEETING.getId());
				assertThat(meeting.getOrganisationId())
						.isEqualTo(MEETING.getOrganisation().getId());
				assertThat(meeting.getName()).isEqualTo(MEETING.getName());
				assertThat(meeting.getDescription())
						.isEqualTo(MEETING.getDescription());
			});

			final var expected = entityManager.find(MeetingDao.class,
					MEETING.getId());
			assertThat(dao).contains(expected);
		}

		@Test
		void meetingIdNonExisting() {
			final Optional<MeetingDao> dao = meetingRepository
					.findByOrganisationIdAndId(
							MEETING.getOrganisation().getId(), Long.MAX_VALUE);

			assertThat(dao).isEmpty();
		}

		@Test
		void organisatioIdNonExisting() {
			final Optional<MeetingDao> dao = meetingRepository
					.findByOrganisationIdAndId(Long.MAX_VALUE, MEETING.getId());

			assertThat(dao).isEmpty();
		}

		@Test
		void meetingBelongsToOtherOrganisation() {
			final long organisationId = TestOrganisation.SPORER.getId();
			final long meetingId = TestMeeting.GLOVER_KICKOFF_2026.getId();

			final Optional<MeetingDao> dao = meetingRepository
					.findByOrganisationIdAndId(organisationId, meetingId);

			assertThat(dao).isEmpty();
		}
	}

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
				assertThat(meeting.getDescription())
						.isEqualTo(expectedMeeting.getDescription());
			});
		}

		@Test
		void multiple() {
			final long organisationId = TestOrganisation.SPORER.getId();

			final List<MeetingDao> result = meetingRepository
					.findAllByOrganisationId(organisationId);

			// TODO: clean up
			assertThat(result).hasSize(3).anySatisfy(meeting -> {
				final TestMeeting expectedMeeting = TestMeeting.SPORER_PROJECT;
				assertThat(meeting.getId()).isEqualTo(expectedMeeting.getId());
				assertThat(meeting.getOrganisationId())
						.isEqualTo(expectedMeeting.getOrganisation().getId());
				assertThat(meeting.getName())
						.isEqualTo(expectedMeeting.getName());
				assertThat(meeting.getDescription())
						.isEqualTo(expectedMeeting.getDescription());
			}).anySatisfy(meeting -> {
				final TestMeeting expectedMeeting = TestMeeting.SPORER_RETRO;
				assertThat(meeting.getId()).isEqualTo(expectedMeeting.getId());
				assertThat(meeting.getOrganisationId())
						.isEqualTo(expectedMeeting.getOrganisation().getId());
				assertThat(meeting.getName())
						.isEqualTo(expectedMeeting.getName());
				assertThat(meeting.getDescription())
						.isEqualTo(expectedMeeting.getDescription());
			}).anySatisfy(meeting -> {
				final TestMeeting expectedMeeting = TestMeeting.SPORER_Q2_PLANNING;
				assertThat(meeting.getId()).isEqualTo(expectedMeeting.getId());
				assertThat(meeting.getOrganisationId())
						.isEqualTo(expectedMeeting.getOrganisation().getId());
				assertThat(meeting.getName())
						.isEqualTo(expectedMeeting.getName());
				assertThat(meeting.getDescription())
						.isEqualTo(expectedMeeting.getDescription());
			});
		}

		@Test
		void notFound() {
			final List<MeetingDao> result = meetingRepository
					.findAllByOrganisationId(Long.MAX_VALUE);

			assertThat(result).isEmpty();
		}
	}

	@Nested
	class Save {

		@Test
		void newMeeting() {
			final TestOrganisation organisation = TestOrganisation.SPORER;
			final String name = "test";

			final var bdo = new MeetingInfo(organisation.getId(), name);
			final var dao = new MeetingDao(bdo);
			final MeetingDao saved = meetingRepository.save(dao);

			assertThat(saved.getId()).isNotNull();
			assertThat(saved.getOrganisationId())
					.isEqualTo(organisation.getId());
			assertThat(saved.getName()).isEqualTo(name);
			assertThat(saved.getDescription()).isEmpty();

			final MeetingDao found = entityManager.find(MeetingDao.class,
					saved.getId());
			assertThat(found).isNotNull();
			assertThat(found.getId()).isEqualTo(saved.getId());
			assertThat(found.getOrganisationId())
					.isEqualTo(saved.getOrganisationId());
			assertThat(found.getName()).isEqualTo(saved.getName());
			assertThat(found.getDescription())
					.isEqualTo(saved.getDescription());
		}

		@Test
		void meetingNull() {
			assertThatThrownBy(() -> meetingRepository.save(null))
					.isInstanceOf(InvalidDataAccessApiUsageException.class);
		}
	}

	@Nested
	class Delete {

		@Test
		void success() throws Exception {
			final TestMeeting meeting = TestMeeting.GLOVER_KICKOFF_2026;
			final MeetingDao dao = new MeetingDao(meeting.info());

			final Field idField = dao.getClass().getDeclaredField("id");
			idField.setAccessible(true);
			idField.set(dao, meeting.getId());

			meetingRepository.delete(dao);

			final MeetingDao found = entityManager.find(MeetingDao.class,
					meeting.getId());

			assertThat(found).isNull();
		}

		@Test
		void nonExisting() throws Exception {
			final TestMeeting meeting = TestMeeting.GLOVER_KICKOFF_2026;
			final MeetingDao dao = new MeetingDao(meeting.info());

			final Field idField = dao.getClass().getDeclaredField("id");
			idField.setAccessible(true);
			idField.set(dao, Long.MAX_VALUE);

			assertThatCode(() -> meetingRepository.delete(dao))
					.doesNotThrowAnyException();

			final MeetingDao found = entityManager.find(MeetingDao.class,
					Long.MAX_VALUE);

			assertThat(found).isNull();
		}
	}
}
