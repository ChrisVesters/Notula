package com.cvesters.notula.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.cvesters.notula.meeting.ChangeService;
import com.cvesters.notula.meeting.TestMeeting;
import com.cvesters.notula.meeting.bdo.MeetingScope;
import com.cvesters.notula.meeting.dto.TopicChangeDto;
import com.cvesters.notula.session.TestSession;
import com.cvesters.notula.test.FrameHandler;
import com.cvesters.notula.test.WebSocketTest;

class SessionOrderWebSocketTest extends WebSocketTest {

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;

	private static final TestMeeting MEETING = TestMeeting.SPORER_PROJECT;

	private static final String ENDPOINT = "/app/meetings/" + MEETING.getId()
			+ "/changes";

	private static final UUID FIRST = UUID
			.fromString("1a2b3c4d-5e6f-4a1b-8c2d-3e4f5a6b7c8d");

	private static final UUID SECOND = UUID
			.fromString("9f8e7d6c-5b4a-4938-8271-6a5b4c3d2e1f");

	@MockitoBean
	private ChangeService changeService;

	private static byte[] change(final long topic) {
		return """
				{ "type": "REMOVE_TOPIC", "topic": %d }
				""".formatted(topic).getBytes(StandardCharsets.UTF_8);
	}

	@Test
	void slowPredecessor() throws Exception {
		final var deleted = new ConcurrentLinkedQueue<Long>();
		final var handled = new CountDownLatch(2);

		doAnswer(invocation -> {
			if (deleted.isEmpty()) {
				Thread.sleep(500);
			}

			final var change = (TopicChangeDto.Remove) invocation
					.getArgument(2);
			deleted.add(change.topic());
			handled.countDown();

			return new MeetingScope(MEETING.getId(), MEETING.getRevision());
		}).when(changeService).apply(any(), anyLong(), any());

		connect(SESSION);
		final FrameHandler rejections = subscribeToRejections();

		send(ENDPOINT, FIRST, change(32));
		send(ENDPOINT, SECOND, change(33));

		assertThat(handled.await(10, TimeUnit.SECONDS)).isTrue();
		assertThat(deleted).containsExactly(32L, 33L);
		assertThat(rejections.getResponse()).isNotDone();
	}
}
