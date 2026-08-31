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
import com.cvesters.notula.common.domain.Origin;
import com.cvesters.notula.common.exception.MissingEntityException;
import com.cvesters.notula.session.TestSession;
import com.cvesters.notula.test.FrameHandler;
import com.cvesters.notula.test.WebSocketTest;
import com.cvesters.notula.topic.TestTopic;

class BlockWebSocketTest extends WebSocketTest {

	private static final String DESTINATION_PREFIX = "/app/blocks";

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
	private static final Origin ORIGIN = new Origin(SESSION.principal(),
			CLIENT_ID);

	private static final TestTopic TOPIC = TestTopic.SPORER_PROJECT_BLOCKERS;

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

			final var action = new BlockAction.Create(TOPIC.getId(),
					BlockType.TEXT, 0);
			final var matcher = new BlockActionMatcher.Create(action);
			verify(blockService, timeout(WAIT_TIMEOUT.toMillis()))
					.create(eq(ORIGIN), argThat(matcher::matches));
		}

		@Test
		void notFound() throws Exception {
			final byte[] payload = getRequestPayload("TEXT", 0);

			when(blockService.create(any(), any()))
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
			return Stream.of(Arguments.of("topicId missing", """
					{
						"type": "TEXT",
						"sequenceId": 0
					}
					"""), Arguments.of("type missing", """
					{
						"topicId": 2,
						"sequenceId": 0
					}
					"""), Arguments.of("type null", """
					{
						"topicId": 2,
						"type": null,
						"sequenceId": 0
					}
					"""), Arguments.of("type invalid", """
					{
						"topicId": 2,
						"type": "UNKNOWN",
						"sequenceId": 0
					}
					"""), Arguments.of("sequenceId missing", """
					{
						"topicId": 2,
						"type": "TEXT"
					}
					"""), Arguments.of("sequenceId negative", """
					{
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
						"topicId": %d,
						"type": "%s",
						"sequenceId": %d
					}
					""".formatted(TOPIC.getId(), type, sequenceId);

			return json.getBytes(StandardCharsets.UTF_8);
		}
	}

	@Nested
	class Move {

		private static final String ENDPOINT = DESTINATION_PREFIX + "/move";

		private static final TestBlock BLOCK = TestBlock.SPORER_PROJECT_BLOCKERS_FIRST;

		@Test
		void success() throws Exception {
			final byte[] payload = getRequestPayload(2);

			connect(SESSION);
			send(ENDPOINT, payload);

			final var action = new BlockAction.Move(2);
			final var matcher = new BlockActionMatcher.Move(action);
			verify(blockService, timeout(WAIT_TIMEOUT.toMillis())).move(
					eq(ORIGIN), eq(BLOCK.getId()),
					argThat(matcher::matches));
		}

		@Test
		void notFound() throws Exception {
			final byte[] payload = getRequestPayload(2);

			when(blockService.move(any(), anyLong(), any()))
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
			final byte[] payload = getRequestPayload(2);

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
			return Stream.of(Arguments.of("blockId missing", """
					{
						"sequenceId": 2
					}
					"""), Arguments.of("sequenceId missing", """
					{
						"blockId": 2
					}
					"""), Arguments.of("sequenceId negative", """
					{
						"blockId": 2,
						"sequenceId": -1
					}
					"""));
		}

		private byte[] getRequestPayload(final int sequenceId) {
			final String json = """
					{
						"blockId": %d,
						"sequenceId": %d
					}
					""".formatted(BLOCK.getId(), sequenceId);

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

			verify(blockService, timeout(WAIT_TIMEOUT.toMillis()))
					.delete(ORIGIN, BLOCK.getId());
		}

		@Test
		void notFound() throws Exception {
			final byte[] payload = getRequestPayload();

			doThrow(new MissingEntityException()).when(blockService)
					.delete(any(), anyLong());

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
			return Stream.of(Arguments.of("blockId missing", """
					{
					}
					"""));
		}

		private byte[] getRequestPayload() {
			final String json = """
					{
						"blockId": %d
					}
					""".formatted(BLOCK.getId());

			return json.getBytes(StandardCharsets.UTF_8);
		}
	}

}
