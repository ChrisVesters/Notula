package com.cvesters.notula.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.context.jdbc.Sql;

import com.cvesters.notula.event.dao.EventDao;
import com.cvesters.notula.meeting.TestMeeting;
import com.cvesters.notula.organisation.TestOrganisation;
import com.cvesters.notula.test.RepositoryTest;

@Sql({ "/db/organisations.sql", "/db/meetings.sql" })
class EventRepositoryTest extends RepositoryTest {

	private static final TestMeeting MEETING = TestMeeting.SPORER_PROJECT;
	private static final TestOrganisation ORGANISATION = MEETING
			.getOrganisation();
	private static final long REVISION = MEETING.getRevision() + 1;

	private static final String PAYLOAD = """
			{"revision":18,"mutation":{"type":"REMOVE_TOPIC","topic":3}}""";

	@Autowired
	private EventRepository eventRepository;

	@Nested
	class Save {

		@Test
		void success() {
			final var dao = new EventDao(ORGANISATION.getId(), MEETING.getId(),
					REVISION, PAYLOAD);

			final EventDao saved = eventRepository.save(dao);
			entityManager.flush();
			entityManager.clear();

			assertThat(saved.getId()).isNotNull();

			final EventDao found = entityManager.find(EventDao.class,
					saved.getId());
			assertThat(found).isNotNull();
			assertThat(found.getOrganisationId())
					.isEqualTo(ORGANISATION.getId());
			assertThat(found.getMeetingId()).isEqualTo(MEETING.getId());
			assertThat(found.getRevision()).isEqualTo(REVISION);
			assertThat(found.getPayload()).isEqualTo("""
					{"mutation": {"type": "REMOVE_TOPIC", "topic": 3}, "revision": 18}""");
		}

		@Test
		void duplicate() {
			eventRepository.save(new EventDao(ORGANISATION.getId(),
					MEETING.getId(), REVISION, PAYLOAD));

			final var dao = new EventDao(ORGANISATION.getId(), MEETING.getId(),
					REVISION, PAYLOAD);

			assertThatThrownBy(() -> eventRepository.save(dao))
					.isInstanceOf(DataIntegrityViolationException.class);
		}

		@Test
		void eventNull() {
			assertThatThrownBy(() -> eventRepository.save(null))
					.isInstanceOf(InvalidDataAccessApiUsageException.class);
		}
	}
}
