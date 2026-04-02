package com.cvesters.notula.meeting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.stomp.ConnectionLostException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.common.exception.MissingEntityException;
import com.cvesters.notula.meeting.bdo.MeetingDetails;
import com.cvesters.notula.meeting.dto.MeetingDetailsDto;
import com.cvesters.notula.session.TestSession;
import com.cvesters.notula.test.FrameHandler;
import com.cvesters.notula.test.WebSocketTest;
import com.cvesters.notula.topic.TestTopic;

class MeetingWebSocketTest extends WebSocketTest {

	private static final String DESTINATION_PREFIX = "/app/meetings/";

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
	private static final Principal PRINCIPAL = SESSION.principal();
	private static final TestMeeting MEETING = TestMeeting.SPORER_PROJECT;
	private static final List<TestTopic> TOPICS = TestTopic.ofMeeting(MEETING);

	@MockitoBean
	private MeetingDetailsService meetingDetailsService;

	@Nested
	class Subscribe {

		@Test
		void success() throws Exception {
			final var details = new MeetingDetails(MEETING.info(),
					TOPICS.stream().map(TestTopic::info).toList());
			when(meetingDetailsService.get(PRINCIPAL, MEETING.getId()))
					.thenReturn(details);

			connect(SESSION);
			final FrameHandler<MeetingDetailsDto> frameHandler = subscribe(
					getDestination(MEETING.getId()), MeetingDetailsDto.class);

			final MeetingDetailsDto response = assertThat(
					frameHandler.getResponse())
							.succeedsWithin(WAIT_TIMEOUT.toSeconds(),
									TimeUnit.SECONDS)
							.isNotNull()
							.actual();

			assertThat(response.info().id()).isEqualTo(MEETING.getId());
			assertThat(response.info().name()).isEqualTo(MEETING.getName());
			assertThat(response.topics()).hasSize(TOPICS.size());

			TOPICS.forEach(topic -> {
				assertThat(response.topics()).anySatisfy(t -> {
					assertThat(t.id()).isEqualTo(topic.getId());
					assertThat(t.name()).isEqualTo(topic.getName());
				});
			});
		}

		@Test
		void notFound() throws Exception {
			when(meetingDetailsService.get(any(), anyLong()))
					.thenThrow(MissingEntityException.class);

			connect(SESSION);
			final FrameHandler<String> errorFrameHandler = subscribeToErrors();
			subscribe(getDestination(MEETING.getId()), MeetingDetailsDto.class);

			assertThat(errorFrameHandler.getResponse())
					.succeedsWithin(WAIT_TIMEOUT.toSeconds(), TimeUnit.SECONDS)
					.isNotNull()
					.isEqualTo("Error");
		}

		@Test
		void unauthenticated() throws Exception {
			connect();
			subscribe(getDestination(MEETING.getId()), MeetingDetailsDto.class);

			assertThat(stompSessionHandler.getError())
					.succeedsWithin(WAIT_TIMEOUT.toSeconds(), TimeUnit.SECONDS)
					.isInstanceOf(ConnectionLostException.class);
		}

		private String getDestination(final long meetingId) {
			return DESTINATION_PREFIX + meetingId;
		}

	}
}
