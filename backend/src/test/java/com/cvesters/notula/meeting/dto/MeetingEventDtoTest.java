package com.cvesters.notula.meeting.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.common.domain.Origin;
import com.cvesters.notula.common.dto.OriginDto;
import com.cvesters.notula.meeting.bdo.MeetingAction;
import com.cvesters.notula.meeting.bdo.MeetingEvent;
import com.cvesters.notula.session.TestSession;

class MeetingEventDtoTest {

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
	private static final UUID CLIENT_ID = UUID
			.fromString("3f9c1a44-1d2e-4a51-8b0c-2c7e9b1d4a07");
	private static final Origin ORIGIN = new Origin(SESSION.principal(),
			CLIENT_ID);

	@Nested
	class Constructor {

		private static final long MEETING_ID = 18L;

		@Test
		void success() {
			final var action = new MeetingAction.Create("New");
			final var event = new MeetingEvent(MEETING_ID, action, ORIGIN);

			final var dto = new MeetingEventDto(event);

			assertThat(dto.getTarget()).isEqualTo("MEETING");
			assertThat(dto.getMeetingId()).isEqualTo(MEETING_ID);
			assertThat(dto.getMutation())
					.isInstanceOf(MeetingMutationDto.Create.class);
			assertThat(dto.getOrigin()).isEqualTo(new OriginDto(ORIGIN));

			final var mutation = (MeetingMutationDto.Create) dto.getMutation();
			assertThat(mutation.getName()).isEqualTo("New");
		}

		@Test
		void eventNull() {
			assertThatThrownBy(() -> new MeetingEventDto(null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
