package com.cvesters.notula.common.messaging;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionSynchronizationUtils;

class TransactionalPublisherTest {

	private static final String DESTINATION = "/topic/meetings/1";
	private static final Object PAYLOAD = new Object();

	private final SimpMessagingTemplate messagingTemplate = mock();
	private final TransactionalPublisher publisher =
			new TransactionalPublisher(messagingTemplate);

	@AfterEach
	void tearDown() {
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.clearSynchronization();
		}
	}

	@Nested
	class Send {

		@Test
		void withoutTransaction() {
			publisher.send(DESTINATION, PAYLOAD);

			verify(messagingTemplate).convertAndSend(DESTINATION, PAYLOAD);
		}

		@Test
		void withinTransaction() {
			TransactionSynchronizationManager.initSynchronization();

			publisher.send(DESTINATION, PAYLOAD);

			verifyNoInteractions(messagingTemplate);

			TransactionSynchronizationUtils.triggerAfterCommit();

			verify(messagingTemplate).convertAndSend(DESTINATION, PAYLOAD);
		}

		@Test
		void rolledBackTransaction() {
			TransactionSynchronizationManager.initSynchronization();

			publisher.send(DESTINATION, PAYLOAD);

			TransactionSynchronizationManager.clearSynchronization();

			verifyNoInteractions(messagingTemplate);
		}

		@Test
		void multiple() {
			TransactionSynchronizationManager.initSynchronization();

			final Object first = new Object();
			final Object second = new Object();

			publisher.send(DESTINATION, first);
			publisher.send(DESTINATION, second);

			TransactionSynchronizationUtils.triggerAfterCommit();

			final var order = inOrder(messagingTemplate);
			order.verify(messagingTemplate).convertAndSend(DESTINATION, first);
			order.verify(messagingTemplate).convertAndSend(DESTINATION,
					second);
		}
	}
}
