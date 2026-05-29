package com.cvesters.notula.textblock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.messaging.simp.stomp.ConnectionLostException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.cvesters.notula.block.TestBlock;
import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.common.exception.MissingEntityException;
import com.cvesters.notula.meeting.TestMeeting;
import com.cvesters.notula.session.TestSession;
import com.cvesters.notula.test.FrameHandler;
import com.cvesters.notula.test.WebSocketTest;
import com.cvesters.notula.textblock.bdo.TextBlockAction;
import com.cvesters.notula.topic.TestTopic;

class TextBlockWebSocketTest extends WebSocketTest {

	private static final String DESTINATION_PREFIX = "/app/meetings";
	private static final String DESTINATION_TOPIC = "/topics";
	private static final String DESTINATION_BLOCK = "/text-blocks";

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
	private static final Principal PRINCIPAL = SESSION.principal();

	private static final TestBlock BLOCK = TestBlock.SPORER_PROJECT_BLOCKERS_FIRST;
	private static final TestTopic TOPIC = BLOCK.getTopic();
	private static final TestMeeting MEETING = TOPIC.getMeeting();

	@MockitoBean
	private TextBlockService textBlockService;

	@Nested
	class UpdateContent {

		private static final TestTopic TOPIC = TestTopic.SPORER_PROJECT_BLOCKERS;

		@ParameterizedTest
		@ValueSource(strings = { "topic", "!@#$%^&*(){}[]|:;'<>,.?/",
				"Встреча: 你好 مرحبا" })
		void success(final String name) throws Exception {
			final byte[] payload = getRequestPayload(name);

			connect(SESSION);
			send(getDestination(MEETING.getId(), TOPIC.getId(), BLOCK.getId()),
					payload);

			final var expected = new TextBlockAction.UpdateContent(5, 2, name);
			final var matcher = new TextBlockActionMatcher.UpdateContent(
					expected);
			verify(textBlockService, timeout(WAIT_TIMEOUT.toMillis())).update(
					eq(PRINCIPAL), eq(MEETING.getId()), eq(TOPIC.getId()),
					eq(BLOCK.getId()), argThat(matcher::matches));
		}

		@Test
		void notFound() throws Exception {
			final byte[] payload = getRequestPayload(TOPIC.getName());

			when(textBlockService.update(any(), anyLong(), anyLong(), anyLong(),
					any())).thenThrow(new MissingEntityException());

			connect(SESSION);
			final FrameHandler errorFrameHandler = subscribeToErrors();
			send(getDestination(MEETING.getId(), TOPIC.getId(), BLOCK.getId()),
					payload);

			assertThat(errorFrameHandler.getResponse())
					.succeedsWithin(WAIT_TIMEOUT.toSeconds(), TimeUnit.SECONDS)
					.isNotNull()
					.satisfies(
							message -> assertThat(message).startsWith("Error"));
		}

		@Test
		void unauthenticated() throws Exception {
			final byte[] payload = getRequestPayload("Updated");

			connect();
			send(getDestination(MEETING.getId(), TOPIC.getId(), BLOCK.getId()),
					payload);

			assertThat(stompSessionHandler.getError())
					.succeedsWithin(WAIT_TIMEOUT.toSeconds(), TimeUnit.SECONDS)
					.isInstanceOf(ConnectionLostException.class);
		}

		private String getDestination(final long meetingId, final long topicId,
				final long blockId) {
			return DESTINATION_PREFIX + "/" + meetingId + DESTINATION_TOPIC
					+ "/" + topicId + "/" + DESTINATION_BLOCK + "/" + blockId;
		}

		private byte[] getRequestPayload(final String name) {
			final String json = """
					{
						"action": "UPDATE_CONTENT",
						"position": 5,
						"length": 2,
						"value": "%s"
					}
					""".formatted(name);

			return json.getBytes(StandardCharsets.UTF_8);
		}
	}
}
