package com.cvesters.notula.textblock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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

	private static final String DESTINATION_PREFIX = "/app/text-blocks";

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
	private static final Principal PRINCIPAL = SESSION.principal();

	private static final TestBlock BLOCK = TestBlock.SPORER_PROJECT_BLOCKERS_FIRST;
	private static final TestTopic TOPIC = BLOCK.getTopic();
	private static final TestMeeting MEETING = TOPIC.getMeeting();
	private static final TestTextBlock TEXT_BLOCK = TestTextBlock.ofBlock(BLOCK);

	@MockitoBean
	private TextBlockService textBlockService;

	@Nested
	class UpdateContent {

		private static final String ENDPOINT = DESTINATION_PREFIX + "/update";

		@ParameterizedTest
		@ValueSource(strings = { "topic", "!@#$%^&*(){}[]|:;'<>,.?/",
				"Встреча: 你好 مرحبا" })
		void success(final String content) throws Exception {
			final byte[] payload = getRequestPayload(content);

			connect(SESSION);
			send(ENDPOINT, payload);

			final var expected = new TextBlockAction.UpdateContent(5, 2,
					content);
			final var matcher = new TextBlockActionMatcher.UpdateContent(
					expected);
			verify(textBlockService, timeout(WAIT_TIMEOUT.toMillis())).update(
					eq(PRINCIPAL), eq(BLOCK.getId()),
					argThat(matcher::matches));
		}

		@Test
		void notFound() throws Exception {
			final byte[] payload = getRequestPayload(TEXT_BLOCK.getContent());

			when(textBlockService.update(any(), anyLong(), any()))
					.thenThrow(new MissingEntityException());

			connect(SESSION);
			final FrameHandler errorFrameHandler = subscribeToErrors();
			send(ENDPOINT, payload);

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
			send(ENDPOINT, payload);

			assertThat(stompSessionHandler.getError())
					.succeedsWithin(WAIT_TIMEOUT.toSeconds(), TimeUnit.SECONDS)
					.isInstanceOf(ConnectionLostException.class);
		}

		@ParameterizedTest(name = "[{index}] name = {0}")
		@MethodSource("invalidPayloadCases")
		void invalidPayload(final String name, final String body)
				throws Exception {
			final byte[] payload = body.getBytes(StandardCharsets.UTF_8);

			connect(SESSION);
			final FrameHandler errorFrameHandler = subscribeToErrors();
			send(ENDPOINT, payload);

			assertThat(errorFrameHandler.getResponse())
					.succeedsWithin(WAIT_TIMEOUT.toSeconds(), TimeUnit.SECONDS)
					.isNotNull()
					.satisfies(
							message -> assertThat(message).startsWith("Error"));

			verifyNoInteractions(textBlockService);
		}

		private static Stream<Arguments> invalidPayloadCases() {
			return Stream.of(Arguments.of("meetingId missing", """
					{
						"topicId": 2,
						"blockId": 3,
						"action": "UPDATE_CONTENT",
						"position": 5,
						"length": 2,
						"value": "content"
					}
					"""), Arguments.of("topicId missing", """
					{
						"meetingId": 1,
						"blockId": 3,
						"action": "UPDATE_CONTENT",
						"position": 5,
						"length": 2,
						"value": "content"
					}
					"""), Arguments.of("blockId missing", """
					{
						"meetingId": 1,
						"topicId": 2,
						"action": "UPDATE_CONTENT",
						"position": 5,
						"length": 2,
						"value": "content"
					}
					"""), Arguments.of("position missing", """
					{
						"meetingId": 1,
						"topicId": 2,
						"blockId": 3,
						"action": "UPDATE_CONTENT",
						"length": 2,
						"value": "content"
					}
					"""), Arguments.of("length missing", """
					{
						"meetingId": 1,
						"topicId": 2,
						"blockId": 3,
						"action": "UPDATE_CONTENT",
						"position": 5,
						"value": "content"
					}
					"""), Arguments.of("value missing", """
					{
						"meetingId": 1,
						"topicId": 2,
						"blockId": 3,
						"action": "UPDATE_CONTENT",
						"position": 5,
						"length": 2
					}
					"""), Arguments.of("position negative", """
					{
						"meetingId": 1,
						"topicId": 2,
						"blockId": 3,
						"action": "UPDATE_CONTENT",
						"position": -5,
						"length": 2,
						"value": "content"
					}
					"""), Arguments.of("length negative", """
					{
						"meetingId": 1,
						"topicId": 2,
						"blockId": 3,
						"action": "UPDATE_CONTENT",
						"position": 5,
						"length": -2,
						"value": "content"
					}
					"""), Arguments.of("value null", """
					{
						"meetingId": 1,
						"topicId": 2,
						"blockId": 3,
						"action": "UPDATE_CONTENT",
						"position": 5,
						"length": 2,
						"value": null
					}
					"""));
		}

		private byte[] getRequestPayload(final String content) {
			final String json = """
					{
						"meetingId": %d,
						"topicId": %d,
						"blockId": %d,
						"action": "UPDATE_CONTENT",
						"position": 5,
						"length": 2,
						"value": "%s"
					}
					""".formatted(MEETING.getId(), TOPIC.getId(), BLOCK.getId(),
					content);

			return json.getBytes(StandardCharsets.UTF_8);
		}
	}
}
