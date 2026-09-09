package com.cvesters.notula.config;

import java.util.UUID;

import org.springframework.core.MethodParameter;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.messaging.support.NativeMessageHeaderAccessor;
import org.springframework.stereotype.Component;

import com.cvesters.notula.common.domain.ChangeId;

@Component
public class ChangeIdArgumentResolver implements HandlerMethodArgumentResolver {

	public static final String CHANGE_ID_HEADER = "change-id";

	@Override
	public boolean supportsParameter(final MethodParameter parameter) {
		return ChangeId.class.equals(parameter.getParameterType());
	}

	@Override
	public ChangeId resolveArgument(final MethodParameter parameter,
			final Message<?> message) {
		final String header = NativeMessageHeaderAccessor
				.getFirstNativeHeader(CHANGE_ID_HEADER, message.getHeaders());
		if (header == null) {
			return ChangeId.NONE;
		}

		try {
			return new ChangeId(UUID.fromString(header));
		} catch (final IllegalArgumentException e) {
			return ChangeId.NONE;
		}
	}
}
