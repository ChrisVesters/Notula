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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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

	private static final String DESTINATION_PREFIX = "/app/meetings";
	private static final String DESTINATION_TOPIC = "/topics";
	private static final String DESTINATION_SUFFIX = "/blocks";

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
	private static final Principal PRINCIPAL = SESSION.principal();

	private static final TestTopic TOPIC = TestTopic.SPORER_PROJECT_BLOCKERS;
	private static final TestMeeting MEETING = TOPIC.getMeeting();

	@MockitoBean
	private BlockService blockService;

	@Nested
	class Create {

		@Test
		void success() throws Exception {
			final byte[] payload = getRequestPayload("TEXT", 0);

			connect(SESSION);
			send(getDestination(MEETING.getId(), TOPIC.getId()), payload);

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
			send(getDestination(MEETING.getId(), TOPIC.getId()), payload);

			assertThat(errorFrameHandler.getResponse())
					.succeedsWithin(WAIT_TIMEOUT.toSeconds(), TimeUnit.SECONDS)
					.isNotNull()
					.satisfies(
							message -> assertThat(message).startsWith("Error"));
		}

		@Test
		void typeNull() throws Exception {
			final byte[] payload = "{\"sequenceId\": 0}"
					.getBytes(StandardCharsets.UTF_8);

			connect(SESSION);
			final FrameHandler errorFrameHandler = subscribeToErrors();
			send(getDestination(MEETING.getId(), TOPIC.getId()), payload);

			verifyNoInteractions(blockService);
			assertThat(errorFrameHandler.getResponse())
					.succeedsWithin(WAIT_TIMEOUT.toSeconds(), TimeUnit.SECONDS)
					.isNotNull()
					.satisfies(
							message -> assertThat(message).startsWith("Error"));
		}

		@Test
		void typeInvalid() throws Exception {
			final byte[] payload = getRequestPayload("UNKNOWN", 0);

			connect(SESSION);
			final FrameHandler errorFrameHandler = subscribeToErrors();
			send(getDestination(MEETING.getId(), TOPIC.getId()), payload);

			verifyNoInteractions(blockService);
			assertThat(errorFrameHandler.getResponse())
					.succeedsWithin(WAIT_TIMEOUT.toSeconds(), TimeUnit.SECONDS)
					.isNotNull()
					.satisfies(
							message -> assertThat(message).startsWith("Error"));
		}

		@Test
		void sequenceIdInvalid() throws Exception {
			final byte[] payload = getRequestPayload("TEXT", -1);

			connect(SESSION);
			final FrameHandler errorFrameHandler = subscribeToErrors();
			send(getDestination(MEETING.getId(), TOPIC.getId()), payload);

			verifyNoInteractions(blockService);
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
			send(getDestination(MEETING.getId(), TOPIC.getId()), payload);

			assertThat(stompSessionHandler.getError())
					.succeedsWithin(WAIT_TIMEOUT.toSeconds(), TimeUnit.SECONDS)
					.isInstanceOf(ConnectionLostException.class);
		}

		private String getDestination(final long meetingId,
				final long topicId) {
			return DESTINATION_PREFIX + "/" + meetingId + DESTINATION_TOPIC
					+ "/" + topicId + DESTINATION_SUFFIX;
		}

		private byte[] getRequestPayload(final String type,
				final int sequenceId) {
			final String json = """
					{
						"type": "%s",
						"sequenceId": %d
					}
					""".formatted(type, sequenceId);

			return json.getBytes(StandardCharsets.UTF_8);
		}
	}

	@Nested
	class Delete {

		private static final TestBlock BLOCK = TestBlock.SPORER_PROJECT_BLOCKERS_FIRST;

		@Test
		void success() throws Exception {
			final byte[] payload = getRequestPayload();

			connect(SESSION);
			send(getDestination(MEETING.getId(), TOPIC.getId(), BLOCK.getId()),
					payload);

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
			final byte[] payload = getRequestPayload();

			connect();
			send(getDestination(MEETING.getId(), TOPIC.getId(), BLOCK.getId()),
					payload);

			assertThat(stompSessionHandler.getError())
					.succeedsWithin(WAIT_TIMEOUT.toSeconds(), TimeUnit.SECONDS)
					.isInstanceOf(ConnectionLostException.class);
		}

		private byte[] getRequestPayload() {
			return new byte[0];
		}

		private String getDestination(final long meetingId, final long topicId,
				final long blockId) {
			return DESTINATION_PREFIX + "/" + meetingId + DESTINATION_TOPIC
					+ "/" + topicId + DESTINATION_SUFFIX + "/" + blockId
					+ "/delete";
		}
	}

}
