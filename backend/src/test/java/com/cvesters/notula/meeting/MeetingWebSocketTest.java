package com.cvesters.notula.meeting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.messaging.simp.stomp.ConnectionLostException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.common.exception.MissingEntityException;
import com.cvesters.notula.meeting.bdo.MeetingAction;
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

	@MockitoBean
	private MeetingService meetingService;

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

	@Nested
	class UpdateName {

		@ParameterizedTest
		@ValueSource(strings = { "meet", "!@#$%^&*(){}[]|\\:;\"'<>,.?/",
				"Встреча: 你好 مرحبا" })
		void success(final String name) throws Exception {
			final Map<String, Object> dto = getDto(name);

			connect(SESSION);
			send(getDestination(MEETING.getId()), dto);

			final var expected = new MeetingAction.UpdateName(5, 2, name);
			final var matcher = new MeetingActionMatcher.UpdateName(expected);
			verify(meetingService, timeout(WAIT_TIMEOUT.toMillis())).update(
					eq(PRINCIPAL), eq(MEETING.getId()),
					argThat(matcher::matches));
		}

		@Test
		void notFound() throws Exception {
			final var meeting = TestMeeting.SPORER_PROJECT;
			final Map<String, Object> dto = getDto(meeting.getName());

			when(meetingService.update(any(), anyLong(), any()))
					.thenThrow(new MissingEntityException());

			connect(SESSION);
			final FrameHandler<String> errorFrameHandler = subscribeToErrors();
			send(getDestination(MEETING.getId()), dto);

			assertThat(errorFrameHandler.getResponse())
					.succeedsWithin(WAIT_TIMEOUT.toSeconds(), TimeUnit.SECONDS)
					.isNotNull()
					.satisfies(message -> message.startsWith("Error"));
		}

		@Test
		void unauthenticated() throws Exception {
			final Map<String, Object> dto = getDto("meeting");

			connect();
			send(getDestination(MEETING.getId()), dto);

			assertThat(stompSessionHandler.getError())
					.succeedsWithin(WAIT_TIMEOUT.toSeconds(), TimeUnit.SECONDS)
					.isInstanceOf(ConnectionLostException.class);
		}

		private Map<String, Object> getDto(final String name) {
			return Map.ofEntries(Map.entry("action", "UPDATE_NAME"),
					Map.entry("position", 5), Map.entry("length", 2),
					Map.entry("value", name));
		}

		private String getDestination(final long meetingId) {
			return DESTINATION_PREFIX + meetingId;
		}
	}
}
