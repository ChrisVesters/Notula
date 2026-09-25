package com.cvesters.notula.event.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Field;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.cvesters.notula.block.bdo.BlockType;
import com.cvesters.notula.common.domain.Minutes;
import com.cvesters.notula.common.domain.Origin;
import com.cvesters.notula.common.domain.Rank;
import com.cvesters.notula.event.EventInfoMatcher;
import com.cvesters.notula.event.bdo.BlockMutation;
import com.cvesters.notula.event.bdo.EventInfo;
import com.cvesters.notula.event.bdo.MeetingMutation;
import com.cvesters.notula.event.bdo.Mutation;
import com.cvesters.notula.event.bdo.TextBlockMutation;
import com.cvesters.notula.event.bdo.TopicMutation;
import com.cvesters.notula.event.dto.TopicMutationDto;
import com.cvesters.notula.meeting.TestMeeting;
import com.cvesters.notula.meeting.bdo.MeetingScope;
import com.cvesters.notula.meeting.dto.TextEditDto;
import com.cvesters.notula.session.TestSession;

class EventDaoTest {

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
	private static final UUID CLIENT_ID = UUID
			.fromString("3f9c1a44-1d2e-4a51-8b0c-2c7e9b1d4a06");
	private static final Origin ORIGIN = new Origin(SESSION.principal(),
			CLIENT_ID);

	private static final TestMeeting MEETING = TestMeeting.SPORER_PROJECT;
	private static final long REVISION = 18L;
	private static final MeetingScope SCOPE = new MeetingScope(MEETING.getId(),
			REVISION);

	private static final long ID = 7L;

	@Nested
	class Constructor {

		@Test
		void success() {
			final var event = new EventInfo(SCOPE, ORIGIN,
					new TopicMutation.Rename(32L, 4, 2, "new"));

			final var dao = new EventDao(event);

			assertThat(dao.getId()).isNull();
			assertThat(dao.getMeetingId()).isEqualTo(MEETING.getId());
			assertThat(dao.getRevision()).isEqualTo(REVISION);
			assertThat(dao.getUserId()).isEqualTo(SESSION.principal().userId());
			assertThat(dao.getClientId()).isEqualTo(CLIENT_ID);
			assertThat(dao.getMutation()).isEqualTo(new TopicMutationDto.Rename(
					32L, new TextEditDto(4, 2, "new")));
		}

		@Test
		void withoutClientId() {
			final var origin = new Origin(SESSION.principal());
			final var event = new EventInfo(SCOPE, origin,
					new TopicMutation.Remove(32L));

			final var dao = new EventDao(event);

			assertThat(dao.getUserId()).isEqualTo(SESSION.principal().userId());
			assertThat(dao.getClientId()).isNull();
		}

		@Test
		void eventNull() {
			assertThatThrownBy(() -> new EventDao(null))
					.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class ToBdo {

		static Stream<Mutation> mutations() {
			return Stream.of(new MeetingMutation.Add("Planning"),
					new MeetingMutation.Rename(4, 2, "new"),
					new MeetingMutation.Describe(4, 2, "new"),
					new MeetingMutation.Remove(),
					new TopicMutation.Add(32L, new Rank("V"), "Blockers"),
					new TopicMutation.Move(32L, new Rank("0V")),
					new TopicMutation.Rename(32L, 4, 2, "new"),
					new TopicMutation.Describe(32L, 4, 2, "new"),
					new TopicMutation.Schedule(32L, new Minutes(15)),
					new TopicMutation.Schedule(32L, null),
					new TopicMutation.Remove(32L),
					new BlockMutation.Add(61L, 32L, BlockType.TEXT,
							new Rank("V")),
					new BlockMutation.Move(61L, new Rank("0V")),
					new BlockMutation.Remove(61L),
					new TextBlockMutation.Edit(61L, 4, 2, "new"));
		}

		@ParameterizedTest
		@MethodSource("mutations")
		void success(final Mutation mutation) throws Exception {
			final var event = new EventInfo(SCOPE, ORIGIN, mutation);
			final var dao = new EventDao(event);
			final Field idField = dao.getClass().getDeclaredField("id");
			idField.setAccessible(true);
			idField.set(dao, ID);

			final EventInfo bdo = dao.toBdo();

			assertThat(bdo.getId()).isEqualTo(ID);
			assertThat(bdo).is(new EventInfoMatcher(event).equal());
		}

		@Test
		void withoutClientId() throws Exception {
			final var origin = new Origin(SESSION.principal());
			final var event = new EventInfo(SCOPE, origin,
					new TopicMutation.Remove(32L));
			final var dao = new EventDao(event);
			final Field idField = dao.getClass().getDeclaredField("id");
			idField.setAccessible(true);
			idField.set(dao, ID);

			final EventInfo bdo = dao.toBdo();

			assertThat(bdo.getId()).isEqualTo(ID);
			assertThat(bdo).is(new EventInfoMatcher(event).equal());
		}

		@Test
		void unsaved() {
			final var event = new EventInfo(SCOPE, ORIGIN,
					new TopicMutation.Remove(32L));
			final var dao = new EventDao(event);

			assertThatThrownBy(dao::toBdo)
					.isInstanceOf(IllegalStateException.class);
		}
	}
}
