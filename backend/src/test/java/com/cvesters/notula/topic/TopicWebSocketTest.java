package com.cvesters.notula.topic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

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
import com.cvesters.notula.meeting.TestMeeting;
import com.cvesters.notula.session.TestSession;
import com.cvesters.notula.test.FrameHandler;
import com.cvesters.notula.test.WebSocketTest;
import com.cvesters.notula.topic.bdo.TopicAction;

public class TopicWebSocketTest extends WebSocketTest {

	private static final String DESTINATION_PREFIX = "/app/meetings/";
	private static final String DESTINATION_SUFFIX = "/topics";

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
	private static final Principal PRINCIPAL = SESSION.principal();
	private static final TestMeeting MEETING = TestMeeting.SPORER_PROJECT;

	@MockitoBean
	private TopicService topicService;

	@Nested
	class Create {

		@ParameterizedTest
		@ValueSource(strings = {"topic", "!@#$%^&*(){}[]|\\:;\"'<>,.?/", "Встреча: 你好 مرحبا"})
		void success(final String name) throws Exception {
			final Map<String, String> dto = Map
					.ofEntries(Map.entry("name", name));

			connect(SESSION);
			send(getDestination(MEETING.getId()), dto);

			final var action = new TopicAction.Create(name);
			verify(topicService, timeout(WAIT_TIMEOUT.toMillis()))
					.create(PRINCIPAL, MEETING.getId(), action);
		}

		@Test
		void notFound() throws Exception {
			final var topic = TestTopic.SPORER_PROJECT_BLOCKERS;
			final Map<String, String> dto = Map
					.ofEntries(Map.entry("name", topic.getName()));

			when(topicService.create(any(), anyLong(), any()))
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
		void invalid() throws Exception {
			final Map<String, String> dto = Map
					.ofEntries(Map.entry("name", ""));

			connect(SESSION);
			final FrameHandler<String> errorFrameHandler = subscribeToErrors();
			send(getDestination(MEETING.getId()), dto);

			verifyNoInteractions(topicService);
			assertThat(errorFrameHandler.getResponse())
					.succeedsWithin(WAIT_TIMEOUT.toSeconds(), TimeUnit.SECONDS)
					.isNotNull()
					.satisfies(message -> message.startsWith("Error"));
		}

		@Test
		void unauthenticated() throws Exception {
			final Map<String, String> dto = Map
					.ofEntries(Map.entry("name", "topic"));

			connect();
			send(getDestination(MEETING.getId()), dto);

			assertThat(stompSessionHandler.getError())
					.succeedsWithin(WAIT_TIMEOUT.toSeconds(), TimeUnit.SECONDS)
					.isInstanceOf(ConnectionLostException.class);
		}

		private String getDestination(final long meetingId) {
			return DESTINATION_PREFIX + meetingId + DESTINATION_SUFFIX;
		}
	}
}
