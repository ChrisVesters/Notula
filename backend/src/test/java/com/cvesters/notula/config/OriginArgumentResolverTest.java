package com.cvesters.notula.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Method;
import java.util.UUID;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.TestingAuthenticationToken;

import com.cvesters.notula.common.domain.Origin;
import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.session.TestSession;

class OriginArgumentResolverTest {

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
	private static final Principal PRINCIPAL = SESSION.principal();

	private static final UUID CLIENT_ID = UUID
			.fromString("3f9c1a44-1d2e-4a51-8b0c-2c7e9b1d4a16");

	private final OriginArgumentResolver resolver = new OriginArgumentResolver();

	@SuppressWarnings("unused")
	private void handler(final Origin origin, final String payload) {
	}

	private static MethodParameter parameter(final int index) throws Exception {
		final Method method = OriginArgumentResolverTest.class
				.getDeclaredMethod("handler", Origin.class, String.class);

		return new MethodParameter(method, index);
	}

	private static Message<String> message(final UUID clientId) {
		final var accessor = StompHeaderAccessor.create(StompCommand.SEND);
		accessor.setUser(SESSION.getAuthToken());

		if (clientId != null) {
			accessor.addNativeHeader(OriginArgumentResolver.CLIENT_ID_HEADER,
					clientId.toString());
		}

		return MessageBuilder.createMessage("", accessor.getMessageHeaders());
	}

	@Nested
	class SupportsParameter {

		@Test
		void origin() throws Exception {
			assertThat(resolver.supportsParameter(parameter(0))).isTrue();
		}

		@Test
		void otherType() throws Exception {
			assertThat(resolver.supportsParameter(parameter(1))).isFalse();
		}
	}

	@Nested
	class ResolveArgument {

		@Test
		void withClientId() throws Exception {
			final Message<String> message = message(CLIENT_ID);

			final Origin origin = resolver.resolveArgument(parameter(0),
					message);

			assertThat(origin.principal()).isEqualTo(PRINCIPAL);
			assertThat(origin.clientId()).isEqualTo(CLIENT_ID);
		}

		@Test
		void withoutClientId() throws Exception {
			final Message<String> message = message(null);

			final Origin origin = resolver.resolveArgument(parameter(0),
					message);

			assertThat(origin.principal()).isEqualTo(PRINCIPAL);
			assertThat(origin.clientId()).isNull();
		}

		@Test
		void invalidClientId() throws Exception {
			final var accessor = StompHeaderAccessor.create(StompCommand.SEND);
			accessor.setUser(SESSION.getAuthToken());
			accessor.addNativeHeader(OriginArgumentResolver.CLIENT_ID_HEADER,
					"not-a-uuid");
			final Message<String> message = MessageBuilder.createMessage("",
					accessor.getMessageHeaders());
			final MethodParameter parameter = parameter(0);

			assertThatThrownBy(
					() -> resolver.resolveArgument(parameter, message))
							.isInstanceOf(IllegalArgumentException.class);
		}

		@Test
		void noUser() throws Exception {
			final var accessor = StompHeaderAccessor.create(StompCommand.SEND);
			final Message<String> message = MessageBuilder.createMessage("",
					accessor.getMessageHeaders());
			final MethodParameter parameter = parameter(0);

			assertThatThrownBy(
					() -> resolver.resolveArgument(parameter, message))
							.isInstanceOf(IllegalStateException.class)
							.hasMessageContaining("not authenticated");
		}

		@Test
		void unsupportedUser() throws Exception {
			final var accessor = StompHeaderAccessor.create(StompCommand.SEND);
			accessor.setUser(mock(java.security.Principal.class));
			final Message<String> message = MessageBuilder.createMessage("",
					accessor.getMessageHeaders());
			final MethodParameter parameter = parameter(0);

			assertThatThrownBy(
					() -> resolver.resolveArgument(parameter, message))
							.isInstanceOf(IllegalStateException.class)
							.hasMessageContaining("Unsupported");
		}

		@Test
		void unsupportedPrincipal() throws Exception {
			final var accessor = StompHeaderAccessor.create(StompCommand.SEND);
			final var principal = new Object();
			final var credentials = new Object();
			final var user = new TestingAuthenticationToken(principal,
					credentials);

			accessor.setUser(user);
			final Message<String> message = MessageBuilder.createMessage("",
					accessor.getMessageHeaders());
			final MethodParameter parameter = parameter(0);

			assertThatThrownBy(
					() -> resolver.resolveArgument(parameter, message))
							.isInstanceOf(IllegalStateException.class)
							.hasMessageContaining("Unsupported");
		}
	}
}
