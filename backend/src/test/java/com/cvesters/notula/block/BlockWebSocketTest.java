package com.cvesters.notula.block;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
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
import org.springframework.messaging.simp.stomp.ConnectionLostException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.cvesters.notula.block.bdo.BlockAction;
import com.cvesters.notula.block.bdo.BlockType;
import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.common.exception.MissingEntityException;
import com.cvesters.notula.meeting.TestMeeting;
import com.cvesters.notula.session.TestSession;
import com.cvesters.notula.test.FrameHandler;
import com.cvesters.notula.test.WebSocketTest;
import com.cvesters.notula.topic.TestTopic;

class BlockWebSocketTest extends WebSocketTest {

	private static final String DESTINATION_PREFIX = "/app/blocks";

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
	private static final Principal PRINCIPAL = SESSION.principal();

	private static final TestTopic TOPIC = TestTopic.SPORER_PROJECT_BLOCKERS;
	private static final TestMeeting MEETING = TOPIC.getMeeting();

	@MockitoBean
	private BlockService blockService;

	@Nested
	class Create {

		private static final String ENDPOINT = DESTINATION_PREFIX + "/create";

		@Test
		void success() throws Exception {
			final byte[] payload = getRequestPayload("TEXT", 0);

			connect(SESSION);
			send(ENDPOINT, payload);

			final var action = new BlockAction.Create(BlockType.TEXT, 0);
			final var matcher = new BlockActionMatcher.Create(action);
			verify(blockService, timeout(WAIT_TIMEOUT.toMillis())).create(
					eq(PRINCIPAL), eq(MEETING.getId()), eq(TOPIC.getId()),
					argThat(matcher::matches));
		}

		@Test
		void notFound() throws Exception {
			final byte[] payload = getRequestPayload("TEXT", 0);

			when(blockService.create(any(), anyLong(), anyLong(), any()))
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
			final byte[] payload = getRequestPayload("TEXT", 0);

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

			verifyNoInteractions(blockService);
		}

		private static Stream<Arguments> invalidPayloadCases() {
			return Stream.of(Arguments.of("meetingId missing", """
					{
						"topicId": 2,
						"type": "TEXT",
						"sequenceId": 0
					}
					"""), Arguments.of("topicId missing", """
					{
						"meetingId": 1,
						"type": "TEXT",
						"sequenceId": 0
					}
					"""), Arguments.of("type missing", """
					{
						"meetingId": 1,
						"topicId": 2,
						"sequenceId": 0
					}
					"""), Arguments.of("type null", """
					{
						"meetingId": 1,
						"topicId": 2,
						"type": null,
						"sequenceId": 0
					}
					"""), Arguments.of("type invalid", """
					{
						"meetingId": 1,
						"topicId": 2,
						"type": "UNKNOWN",
						"sequenceId": 0
					}
					"""), Arguments.of("sequenceId missing", """
					{
						"meetingId": 1,
						"topicId": 2,
						"type": "TEXT"
					}
					"""), Arguments.of("sequenceId negative", """
					{
						"meetingId": 1,
						"topicId": 2,
						"type": "TEXT",
						"sequenceId": -1
					}
					"""));
		}

		private byte[] getRequestPayload(final String type,
				final int sequenceId) {
			final String json = """
					{
						"meetingId": %d,
						"topicId": %d,
						"type": "%s",
						"sequenceId": %d
					}
					""".formatted(MEETING.getId(), TOPIC.getId(), type,
					sequenceId);

			return json.getBytes(StandardCharsets.UTF_8);
		}
	}

	@Nested
	class Delete {

		private static final String ENDPOINT = DESTINATION_PREFIX + "/delete";

		private static final TestBlock BLOCK = TestBlock.SPORER_PROJECT_BLOCKERS_FIRST;

		@Test
		void success() throws Exception {
			final byte[] payload = getRequestPayload();

			connect(SESSION);
			send(ENDPOINT, payload);

			verify(blockService, timeout(WAIT_TIMEOUT.toMillis())).delete(
					PRINCIPAL, MEETING.getId(), TOPIC.getId(), BLOCK.getId());
		}

		@Test
		void notFound() throws Exception {
			final byte[] payload = getRequestPayload();

			doThrow(new MissingEntityException()).when(blockService)
					.delete(any(), anyLong(), anyLong(), anyLong());

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
			final byte[] payload = getRequestPayload();

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

			verifyNoInteractions(blockService);
		}

		private static Stream<Arguments> invalidPayloadCases() {
			return Stream.of(Arguments.of("meetingId missing", """
					{
						"topicId": 2,
						"blockId": 3
					}
					"""), Arguments.of("topicId missing", """
					{
						"meetingId": 1,
						"blockId": 3
					}
					"""), Arguments.of("blockId missing", """
					{
						"meetingId": 1,
						"topicId": 2
					}
					"""));
		}

		private byte[] getRequestPayload() {
			final String json = """
					{
						"meetingId": %d,
						"topicId": %d,
						"blockId": %d
					}
					""".formatted(MEETING.getId(), TOPIC.getId(),
					BLOCK.getId());

			return json.getBytes(StandardCharsets.UTF_8);
		}
	}

}
