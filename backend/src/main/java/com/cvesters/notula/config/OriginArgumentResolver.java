package com.cvesters.notula.config;

import java.util.Optional;
import java.util.UUID;

import org.springframework.core.MethodParameter;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.support.NativeMessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.cvesters.notula.common.domain.Origin;
import com.cvesters.notula.common.domain.Principal;

@Component
public class OriginArgumentResolver implements HandlerMethodArgumentResolver {

	public static final String CLIENT_ID_HEADER = "client-id";

	@Override
	public boolean supportsParameter(final MethodParameter parameter) {
		return Origin.class.equals(parameter.getParameterType());
	}

	@Override
	public Origin resolveArgument(final MethodParameter parameter,
			final Message<?> message) {
		final Principal principal = getPrincipal(message);
		final UUID clientId = getClientId(message);

		return new Origin(principal, clientId);
	}

	private Principal getPrincipal(final Message<?> message) {
		final var user = SimpMessageHeaderAccessor
				.getUser(message.getHeaders());
		if (user == null) {
			throw new IllegalStateException("User not authenticated");
		}

		if (user instanceof Authentication authentication && authentication
				.getPrincipal() instanceof Principal principal) {
			return principal;
		}

		throw new IllegalStateException(
				"Unsupported Authentication principal type: "
						+ user.getClass().getName());
	}

	private UUID getClientId(final Message<?> message) {
		final String clientId = NativeMessageHeaderAccessor
				.getFirstNativeHeader(CLIENT_ID_HEADER, message.getHeaders());

		return Optional.ofNullable(clientId).map(UUID::fromString).orElse(null);
	}
}
