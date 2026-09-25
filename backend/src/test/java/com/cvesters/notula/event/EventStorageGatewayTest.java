package com.cvesters.notula.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.common.domain.Origin;
import com.cvesters.notula.event.bdo.EventInfo;
import com.cvesters.notula.event.bdo.TopicMutation;
import com.cvesters.notula.event.dao.EventDao;
import com.cvesters.notula.event.dto.TopicMutationDto;
import com.cvesters.notula.meeting.bdo.MeetingScope;
import com.cvesters.notula.session.TestSession;

class EventStorageGatewayTest {

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
	private static final UUID CLIENT_ID = UUID
			.fromString("3f9c1a44-1d2e-4a51-8b0c-2c7e9b1d4a06");
	private static final Origin ORIGIN = new Origin(SESSION.principal(),
			CLIENT_ID);
	private static final long MEETING_ID = 1L;
	private static final long REVISION = 18L;
	private static final MeetingScope SCOPE = new MeetingScope(MEETING_ID,
			REVISION);
	private static final UUID CHANGE_ID = UUID
			.fromString("7c6f0d54-2f70-4a1e-9f5a-1d4c8b2e0a11");

	private final EventRepository eventRepository = mock();

	private final EventStorageGateway gateway = new EventStorageGateway(
			eventRepository);

	@Nested
	class Create {

		@Test
		void success() {
			final var event = new EventInfo(SCOPE, ORIGIN,
					new TopicMutation.Remove(32L));

			final EventDao saved = mock();
			final EventInfo created = mock();
			when(saved.toBdo()).thenReturn(created);

			when(eventRepository.save(argThat(dao -> {
				assertThat(dao.getId()).isNull();
				assertThat(dao.getMeetingId()).isEqualTo(MEETING_ID);
				assertThat(dao.getRevision()).isEqualTo(REVISION);
				assertThat(dao.getUserId())
						.isEqualTo(SESSION.principal().userId());
				assertThat(dao.getClientId()).isEqualTo(CLIENT_ID);
				assertThat(dao.getMutation())
						.isEqualTo(new TopicMutationDto.Remove(32L));
				return true;
			}))).thenReturn(saved);

			final EventInfo result = gateway.create(event);

			assertThat(result).isEqualTo(created);
		}

		@Test
		void eventNull() {
			assertThatThrownBy(() -> gateway.create(null))
					.isInstanceOf(NullPointerException.class);

			verifyNoInteractions(eventRepository);
		}
	}

	@Nested
	class FindAllSince {

		@Test
		void success() {
			final EventDao first = mock();
			final EventDao second = mock();
			final EventInfo firstInfo = mock();
			final EventInfo secondInfo = mock();
			when(first.toBdo()).thenReturn(firstInfo);
			when(second.toBdo()).thenReturn(secondInfo);
			when(eventRepository.findAllSince(MEETING_ID, REVISION))
					.thenReturn(List.of(first, second));

			final List<EventInfo> events = gateway.findAllSince(MEETING_ID,
					REVISION);

			assertThat(events).containsExactly(firstInfo, secondInfo);
		}

		@Test
		void none() {
			when(eventRepository.findAllSince(MEETING_ID, REVISION))
					.thenReturn(List.of());

			final List<EventInfo> events = gateway.findAllSince(MEETING_ID,
					REVISION);

			assertThat(events).isEmpty();
		}
	}

	@Nested
	class FindByChangeId {

		@Test
		void success() {
			final EventDao dao = mock();
			final EventInfo event = mock();
			when(dao.toBdo()).thenReturn(event);
			when(eventRepository.findByMeetingIdAndChangeId(MEETING_ID,
					CHANGE_ID)).thenReturn(Optional.of(dao));

			final Optional<EventInfo> found = gateway.findByChangeId(MEETING_ID,
					CHANGE_ID);

			assertThat(found).contains(event);
		}

		@Test
		void none() {
			when(eventRepository.findByMeetingIdAndChangeId(MEETING_ID,
					CHANGE_ID)).thenReturn(Optional.empty());

			final Optional<EventInfo> found = gateway.findByChangeId(MEETING_ID,
					CHANGE_ID);

			assertThat(found).isEmpty();
		}

		@Test
		void changeIdNull() {
			assertThatThrownBy(() -> gateway.findByChangeId(MEETING_ID, null))
					.isInstanceOf(NullPointerException.class);

			verifyNoInteractions(eventRepository);
		}
	}
}
