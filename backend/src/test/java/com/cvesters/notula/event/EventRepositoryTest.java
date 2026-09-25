package com.cvesters.notula.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.context.jdbc.Sql;

import com.cvesters.notula.block.bdo.BlockType;
import com.cvesters.notula.common.domain.Minutes;
import com.cvesters.notula.common.domain.Origin;
import com.cvesters.notula.common.domain.Rank;
import com.cvesters.notula.common.domain.Splice;
import com.cvesters.notula.event.bdo.BlockMutation;
import com.cvesters.notula.event.bdo.EventInfo;
import com.cvesters.notula.event.bdo.MeetingMutation;
import com.cvesters.notula.event.bdo.Mutation;
import com.cvesters.notula.event.bdo.TextBlockMutation;
import com.cvesters.notula.event.bdo.TopicMutation;
import com.cvesters.notula.event.dao.EventDao;
import com.cvesters.notula.event.dto.TopicMutationDto;
import com.cvesters.notula.meeting.TestMeeting;
import com.cvesters.notula.meeting.bdo.MeetingScope;
import com.cvesters.notula.session.TestSession;
import com.cvesters.notula.test.RepositoryTest;

@Sql({ "/db/users.sql", "/db/organisations.sql", "/db/meetings.sql" })
class EventRepositoryTest extends RepositoryTest {

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
	private static final UUID CLIENT_ID = UUID
			.fromString("3f9c1a44-1d2e-4a51-8b0c-2c7e9b1d4a06");
	private static final Origin ORIGIN = new Origin(SESSION.principal(),
			CLIENT_ID);

	private static final TestMeeting MEETING = TestMeeting.SPORER_PROJECT;
	private static final long REVISION = MEETING.getRevision() + 1;
	private static final MeetingScope SCOPE = new MeetingScope(MEETING.getId(),
			REVISION);

	private static final EventInfo EVENT = new EventInfo(SCOPE, ORIGIN,
			new TopicMutation.Remove(3L));

	@Autowired
	private EventRepository eventRepository;

	@Nested
	class Save {

		@Test
		void success() {
			final EventDao saved = eventRepository.save(new EventDao(EVENT));
			entityManager.flush();
			entityManager.clear();

			assertThat(saved.getId()).isNotNull();

			final EventDao found = entityManager.find(EventDao.class,
					saved.getId());
			assertThat(found).isNotNull();
			assertThat(found.getMeetingId()).isEqualTo(MEETING.getId());
			assertThat(found.getRevision()).isEqualTo(REVISION);
			assertThat(found.getUserId())
					.isEqualTo(SESSION.principal().userId());
			assertThat(found.getClientId()).isEqualTo(CLIENT_ID);
			assertThat(found.getMutation())
					.isEqualTo(new TopicMutationDto.Remove(3L));
			assertThat(found.toBdo()).is(new EventInfoMatcher(EVENT).equal());

			final Object stored = entityManager
					.createNativeQuery(
							"SELECT mutation::text FROM events WHERE id = ?")
					.setParameter(1, saved.getId())
					.getSingleResult();
			assertThat(stored).isEqualTo("""
					{"type": "REMOVE_TOPIC", "topic": 3}""");
		}

		static Stream<Mutation> mutations() {
			return Stream.of(new MeetingMutation.Add("Planning"),
					new MeetingMutation.Rename(new Splice(4, 2, "new")),
					new MeetingMutation.Describe(new Splice(4, 2, "new")),
					new MeetingMutation.Remove(),
					new TopicMutation.Add(32L, new Rank("V"), "Blockers"),
					new TopicMutation.Move(32L, new Rank("0V")),
					new TopicMutation.Rename(32L, new Splice(4, 2, "new")),
					new TopicMutation.Describe(32L, new Splice(4, 2, "new")),
					new TopicMutation.Schedule(32L, new Minutes(15)),
					new TopicMutation.Schedule(32L, null),
					new TopicMutation.Remove(32L),
					new BlockMutation.Add(61L, 32L, BlockType.TEXT,
							new Rank("V")),
					new BlockMutation.Move(61L, new Rank("0V")),
					new BlockMutation.Remove(61L),
					new TextBlockMutation.Edit(61L, new Splice(4, 2, "new")));
		}

		@ParameterizedTest
		@MethodSource("mutations")
		void mutation(final Mutation mutation) {
			final var event = new EventInfo(SCOPE, ORIGIN, mutation);

			final EventDao saved = eventRepository.save(new EventDao(event));
			entityManager.flush();
			entityManager.clear();

			final EventDao found = entityManager.find(EventDao.class,
					saved.getId());
			assertThat(found.toBdo()).is(new EventInfoMatcher(event).equal());
		}

		@Test
		void duplicate() {
			eventRepository.save(new EventDao(EVENT));

			final var dao = new EventDao(EVENT);

			assertThatThrownBy(() -> eventRepository.save(dao))
					.isInstanceOf(DataIntegrityViolationException.class);
		}

		@Test
		void eventNull() {
			assertThatThrownBy(() -> eventRepository.save(null))
					.isInstanceOf(InvalidDataAccessApiUsageException.class);
		}
	}

	@Nested
	class FindAllSince {

		@Test
		void success() {
			final long meetingId = MEETING.getId();
			final long otherMeetingId = TestMeeting.SPORER_RETRO.getId();
			eventRepository.save(new EventDao(new EventInfo(
					new MeetingScope(meetingId, REVISION + 2), ORIGIN,
					new TopicMutation.Remove(5L))));
			eventRepository.save(new EventDao(new EventInfo(
					new MeetingScope(meetingId, REVISION), ORIGIN,
					new TopicMutation.Remove(3L))));
			eventRepository.save(new EventDao(new EventInfo(
					new MeetingScope(meetingId, REVISION + 1), ORIGIN,
					new TopicMutation.Remove(4L))));
			eventRepository.save(new EventDao(new EventInfo(
					new MeetingScope(otherMeetingId, REVISION + 1), ORIGIN,
					new TopicMutation.Remove(6L))));
			entityManager.flush();
			entityManager.clear();

			final List<EventDao> found = eventRepository.findAllSince(meetingId,
					REVISION);

			assertThat(found).extracting(EventDao::getRevision)
					.containsExactly(REVISION + 1, REVISION + 2);
			assertThat(found).extracting(EventDao::getMeetingId)
					.containsOnly(meetingId);
		}

		@Test
		void none() {
			eventRepository.save(new EventDao(EVENT));
			entityManager.flush();
			entityManager.clear();

			final List<EventDao> found = eventRepository
					.findAllSince(MEETING.getId(), REVISION);

			assertThat(found).isEmpty();
		}
	}
}
