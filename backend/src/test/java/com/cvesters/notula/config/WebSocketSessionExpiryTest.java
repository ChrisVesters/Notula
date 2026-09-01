package com.cvesters.notula.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.stomp.ConnectionLostException;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import com.cvesters.notula.meeting.TestMeeting;
import com.cvesters.notula.session.TestSession;
import com.cvesters.notula.test.WebSocketTest;

class WebSocketSessionExpiryTest extends WebSocketTest {

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;

	@MockitoSpyBean
	private TaskScheduler webSocketTaskScheduler;

	@Test
	void closedWhenTokenExpires() throws Exception {
		final var expiresAt = Instant.now().plusSeconds(3600);

		connect(SESSION, expiresAt);

		final var task = ArgumentCaptor.forClass(Runnable.class);
		final var scheduled = ArgumentCaptor.forClass(Instant.class);
		verify(webSocketTaskScheduler, timeout(WAIT_TIMEOUT.toMillis()))
				.schedule(task.capture(), scheduled.capture());

		assertThat(scheduled.getValue()).isEqualTo(expiresAt);

		task.getValue().run();

		assertThat(stompSessionHandler.getError())
				.succeedsWithin(WAIT_TIMEOUT.toSeconds(), TimeUnit.SECONDS)
				.isInstanceOf(ConnectionLostException.class);
	}

	@Test
	void unauthenticatedWhenTokenHasNoExpiry() throws Exception {
		connect(SESSION, null);
		subscribe("/app/meetings/" + TestMeeting.SPORER_PROJECT.getId());

		verify(webSocketTaskScheduler,
				timeout(WAIT_TIMEOUT.toMillis()).times(0))
						.schedule(any(Runnable.class), any(Instant.class));

		assertThat(stompSessionHandler.getError())
				.succeedsWithin(WAIT_TIMEOUT.toSeconds(), TimeUnit.SECONDS)
				.isInstanceOf(ConnectionLostException.class);
	}
}
