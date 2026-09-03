package com.cvesters.notula.config;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.annotation.support.SimpAnnotationMethodMessageHandler;
import org.springframework.messaging.support.ExecutorChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
public class SessionOrderInterceptor implements ExecutorChannelInterceptor {

	private final SessionOrder order;

	public SessionOrderInterceptor(final SessionOrder order) {
		this.order = order;
	}

	@Override
	public Message<?> preSend(final Message<?> message,
			final MessageChannel channel) {
		final MessageHeaders headers = message.getHeaders();

		final String sessionId = SimpMessageHeaderAccessor
				.getSessionId(headers);
		if (sessionId == null) {
			return message;
		}

		if (SimpMessageType.DISCONNECT == SimpMessageHeaderAccessor
				.getMessageType(headers)) {
			order.forget(sessionId);
		} else if (isAction(headers)) {
			order.acquire(sessionId);
		}

		return message;
	}

	@Override
	public void afterMessageHandled(final Message<?> message,
			final MessageChannel channel, final MessageHandler handler,
			final Exception ex) {
		if (!(handler instanceof SimpAnnotationMethodMessageHandler)) {
			return;
		}

		final MessageHeaders headers = message.getHeaders();

		final String sessionId = SimpMessageHeaderAccessor
				.getSessionId(headers);
		if (sessionId != null && isAction(headers)) {
			order.release(sessionId);
		}
	}

	private static boolean isAction(final MessageHeaders headers) {
		if (SimpMessageType.MESSAGE != SimpMessageHeaderAccessor
				.getMessageType(headers)) {
			return false;
		}

		final String destination = SimpMessageHeaderAccessor
				.getDestination(headers);

		return destination != null
				&& destination.startsWith(WebSocketConfig.APPLICATION_PREFIX);
	}
}
