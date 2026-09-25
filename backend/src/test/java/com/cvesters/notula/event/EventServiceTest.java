package com.cvesters.notula.event;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.common.domain.Origin;
import com.cvesters.notula.common.domain.Splice;
import com.cvesters.notula.common.dto.OriginDto;
import com.cvesters.notula.common.messaging.TransactionalPublisher;
import com.cvesters.notula.event.bdo.EventInfo;
import com.cvesters.notula.event.bdo.TopicMutation;
import com.cvesters.notula.event.dto.EventDto;
import com.cvesters.notula.event.dto.TopicMutationDto;
import com.cvesters.notula.meeting.bdo.MeetingScope;
import com.cvesters.notula.meeting.dto.TextEditDto;
import com.cvesters.notula.session.TestSession;

class EventServiceTest {

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
	private static final UUID CLIENT_ID = UUID
			.fromString("3f9c1a44-1d2e-4a51-8b0c-2c7e9b1d4a0b");
	private static final Origin ORIGIN = new Origin(SESSION.principal(),
			CLIENT_ID);
	private static final long USER_ID = SESSION.principal().userId();

	private static final long ID = 7L;
	private static final long MEETING_ID = 1L;
	private static final long REVISION = 12L;
	private static final MeetingScope SCOPE = new MeetingScope(MEETING_ID,
			REVISION);
	private static final long TOPIC_ID = 32L;

	private static final String DESTINATION = "/topic/meetings/" + MEETING_ID;

	private final TransactionalPublisher publisher = mock();
	private final EventStorageGateway eventStorage = mock();
	private final EventService eventService = new EventService(publisher,
			eventStorage);

	@Nested
	class Publish {

		@Test
		void success() {
			final var mutation = new TopicMutation.Rename(TOPIC_ID,
					new Splice(4, 12, "Updated"));
			final var event = new EventInfo(SCOPE, ORIGIN, mutation);
			final var created = new EventInfo(ID, MEETING_ID, REVISION,
					USER_ID, CLIENT_ID, mutation);
			when(eventStorage.create(event)).thenReturn(created);

			eventService.publish(event);

			final var edit = new TextEditDto(4, 12, "Updated");
			verify(publisher).send(DESTINATION,
					new EventDto(REVISION, new OriginDto(ORIGIN),
							new TopicMutationDto.Rename(TOPIC_ID, edit)));
		}

		@Test
		void eventNull() {
			assertThatThrownBy(() -> eventService.publish(null))
					.isInstanceOf(NullPointerException.class);

			verifyNoInteractions(eventStorage, publisher);
		}
	}
}
