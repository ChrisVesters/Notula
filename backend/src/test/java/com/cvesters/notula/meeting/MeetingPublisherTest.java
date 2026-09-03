package com.cvesters.notula.meeting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.UUID;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.common.domain.Origin;
import com.cvesters.notula.common.dto.OriginDto;
import com.cvesters.notula.common.messaging.TransactionalPublisher;
import com.cvesters.notula.meeting.bdo.MeetingAction;
import com.cvesters.notula.meeting.bdo.MeetingEvent;
import com.cvesters.notula.meeting.dto.MeetingEventDto;
import com.cvesters.notula.meeting.dto.MeetingMutationDto;
import com.cvesters.notula.session.TestSession;

class MeetingPublisherTest {

	private static final String DESTINATION_PREFIX = "/topic/meetings";

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
	private static final UUID CLIENT_ID = UUID
			.fromString("3f9c1a44-1d2e-4a51-8b0c-2c7e9b1d4a0b");
	private static final Origin ORIGIN = new Origin(SESSION.principal(),
			CLIENT_ID);

	private final TransactionalPublisher publisher = mock();
	private final MeetingPublisher meetingPublisher = new MeetingPublisher(
			publisher);

	@Nested
	class Publish {

		private static final long MEETING_ID = 1L;

		private static final String DESTINATION = DESTINATION_PREFIX + "/"
				+ MEETING_ID;

		@Test
		void create() {
			final var action = new MeetingAction.Create("New");
			final var event = new MeetingEvent(MEETING_ID, action, ORIGIN);

			meetingPublisher.publish(event);

			verify(publisher).send(eq(DESTINATION),
					argThat((MeetingEventDto dto) -> {
						assertThat(dto.getMeetingId()).isEqualTo(MEETING_ID);
						assertThat(dto.getOrigin())
								.isEqualTo(new OriginDto(ORIGIN));
						assertThat(dto.getMutation())
								.isInstanceOf(MeetingMutationDto.Create.class);

						final var mutation = (MeetingMutationDto.Create) dto
								.getMutation();
						assertThat(mutation.getName()).isEqualTo("New");
						return true;
					}));
		}

		@Test
		void updateName() {
			final var action = new MeetingAction.UpdateName(4, 12, "Updated");
			final var event = new MeetingEvent(MEETING_ID, action, ORIGIN);

			meetingPublisher.publish(event);

			verify(publisher).send(eq(DESTINATION),
					argThat((MeetingEventDto dto) -> {
						assertThat(dto.getMeetingId()).isEqualTo(MEETING_ID);
						assertThat(dto.getMutation()).isInstanceOf(
								MeetingMutationDto.UpdateName.class);

						final var mutation = (MeetingMutationDto.UpdateName) dto
								.getMutation();
						assertThat(mutation.getPosition()).isEqualTo(4);
						assertThat(mutation.getLength()).isEqualTo(12);
						assertThat(mutation.getValue()).isEqualTo("Updated");
						return true;
					}));
		}

		@Test
		void updateDescription() {
			final var action = new MeetingAction.UpdateDescription(4, 12,
					"Updated");
			final var event = new MeetingEvent(MEETING_ID, action, ORIGIN);

			meetingPublisher.publish(event);

			verify(publisher).send(eq(DESTINATION),
					argThat((MeetingEventDto dto) -> {
						assertThat(dto.getMeetingId()).isEqualTo(MEETING_ID);
						assertThat(dto.getMutation()).isInstanceOf(
								MeetingMutationDto.UpdateDescription.class);

						final var mutation = (MeetingMutationDto.UpdateDescription) dto
								.getMutation();
						assertThat(mutation.getPosition()).isEqualTo(4);
						assertThat(mutation.getLength()).isEqualTo(12);
						assertThat(mutation.getValue()).isEqualTo("Updated");
						return true;
					}));
		}

		@Test
		void delete() {
			final var action = new MeetingAction.Delete();
			final var event = new MeetingEvent(MEETING_ID, action, ORIGIN);

			meetingPublisher.publish(event);

			verify(publisher).send(eq(DESTINATION),
					argThat((MeetingEventDto dto) -> {
						assertThat(dto.getMeetingId()).isEqualTo(MEETING_ID);
						assertThat(dto.getMutation())
								.isInstanceOf(MeetingMutationDto.Delete.class);
						return true;
					}));
		}

		@Test
		void eventNull() {
			assertThatThrownBy(() -> meetingPublisher.publish(null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
