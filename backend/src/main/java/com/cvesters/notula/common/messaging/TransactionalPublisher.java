package com.cvesters.notula.common.messaging;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class TransactionalPublisher {

	private final SimpMessagingTemplate messagingTemplate;

	public TransactionalPublisher(
			final SimpMessagingTemplate messagingTemplate) {
		this.messagingTemplate = messagingTemplate;
	}

	public void send(final String destination, final Object payload) {
		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			messagingTemplate.convertAndSend(destination, payload);

			return;
		}

		TransactionSynchronizationManager
				.registerSynchronization(new TransactionSynchronization() {

					@Override
					public void afterCommit() {
						messagingTemplate.convertAndSend(destination, payload);
					}
				});
	}
}
