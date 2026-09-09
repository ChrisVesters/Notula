package com.cvesters.notula.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.UUID;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;

import com.cvesters.notula.common.domain.ChangeId;

class ChangeIdArgumentResolverTest {

	private static final UUID CHANGE_ID = UUID
			.fromString("7c6f0d54-2f70-4a1e-9f5a-1d4c8b2e0a11");

	private final ChangeIdArgumentResolver resolver =
			new ChangeIdArgumentResolver();

	@SuppressWarnings("unused")
	private void handler(final ChangeId change, final String payload) {
	}

	private static MethodParameter parameter(final int index) throws Exception {
		final Method method = ChangeIdArgumentResolverTest.class
				.getDeclaredMethod("handler", ChangeId.class, String.class);

		return new MethodParameter(method, index);
	}

	private static Message<String> message(final String changeId) {
		final var accessor = StompHeaderAccessor.create(StompCommand.SEND);

		if (changeId != null) {
			accessor.addNativeHeader(
					ChangeIdArgumentResolver.CHANGE_ID_HEADER, changeId);
		}

		return MessageBuilder.createMessage("", accessor.getMessageHeaders());
	}

	@Nested
	class SupportsParameter {

		@Test
		void changeId() throws Exception {
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
		void named() throws Exception {
			final Message<String> message = message(CHANGE_ID.toString());

			final ChangeId change = resolver.resolveArgument(parameter(0),
					message);

			assertThat(change.value()).isEqualTo(CHANGE_ID);
		}

		@Test
		void unnamed() throws Exception {
			final Message<String> message = message(null);

			final ChangeId change = resolver.resolveArgument(parameter(0),
					message);

			assertThat(change.value()).isNull();
		}

		@Test
		void unreadable() throws Exception {
			final Message<String> message = message("not-a-uuid");

			final ChangeId change = resolver.resolveArgument(parameter(0),
					message);

			assertThat(change.value()).isNull();
		}
	}
}
