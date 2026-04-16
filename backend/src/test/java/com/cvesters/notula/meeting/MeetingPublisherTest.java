package com.cvesters.notula.meeting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.cvesters.notula.meeting.bdo.MeetingAction;
import com.cvesters.notula.meeting.bdo.MeetingEvent;
import com.cvesters.notula.meeting.dto.MeetingEventDto;

class MeetingPublisherTest {

	private static final String DESTINATION_PREFIX = "/topic/meetings";

	private final SimpMessagingTemplate messagingTemplate = mock();
	private final MeetingPublisher publisher = new MeetingPublisher(
			messagingTemplate);

	@Nested
	class Publish {

		private static final long MEETING_ID = 1L;

		private static final String DESTINATION = DESTINATION_PREFIX + "/"
				+ MEETING_ID;

		@Test
		void create() {
			final var action = new MeetingAction.Create("New");
			final var event = new MeetingEvent(MEETING_ID, action);

			publisher.publish(event);

			verify(messagingTemplate).convertAndSend(eq(DESTINATION),
					argThat((MeetingEventDto dto) -> {
						assertThat(dto)
								.isInstanceOf(MeetingEventDto.Create.class);

						final var createDto = (MeetingEventDto.Create) dto;
						assertThat(createDto.getName()).isEqualTo("New");
						return true;
					}));
		}

		@Test
		void updateName() {
			final var action = new MeetingAction.UpdateName(4, 12, "Updated");
			final var event = new MeetingEvent(MEETING_ID, action);

			publisher.publish(event);

			verify(messagingTemplate).convertAndSend(eq(DESTINATION),
					argThat((MeetingEventDto dto) -> {
						assertThat(dto)
								.isInstanceOf(MeetingEventDto.UpdateName.class);

						final var updateDto = (MeetingEventDto.UpdateName) dto;
						assertThat(updateDto.getPosition()).isEqualTo(4);
						assertThat(updateDto.getLength()).isEqualTo(12);
						assertThat(updateDto.getValue()).isEqualTo("Updated");
						return true;
					}));
		}

		@Test
		void eventNull() {
			assertThatThrownBy(() -> publisher.publish(null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
