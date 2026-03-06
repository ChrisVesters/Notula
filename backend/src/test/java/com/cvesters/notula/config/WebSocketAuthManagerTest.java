package com.cvesters.notula.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.messaging.access.intercept.MessageAuthorizationContext;

import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.session.TestSession;
import com.cvesters.notula.user.TestUser;

class WebSocketAuthManagerTest {

	private static final String PATTERN = "TEST/*";

	private static final Authentication auth = mock();
	private static final MessageAuthorizationContext<?> context = mock();

	private final WebSocketAuthManager manager = new TestWebSocketAuthManager();

	@Nested
	class Authorize {

		@Test
		void authenticationNull() {
			final AuthorizationResult result = manager.authorize(() -> null,
					context);

			assertThat(result).isNull();
		}

		@Test
		void principalNull() {
			when(auth.getPrincipal()).thenReturn(null);

			final AuthorizationResult result = manager.authorize(() -> auth,
					context);

			assertThat(result).isNull();
		}

		@Test
		void principalWrongType() {
			when(auth.getPrincipal()).thenReturn("ADMIN");

			final AuthorizationResult result = manager.authorize(() -> auth,
					context);

			assertThat(result).isNull();
		}

		@Test
		void hasAccess() {
			final var session = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
			when(auth.getPrincipal()).thenReturn(session.principal());

			final AuthorizationResult result = manager.authorize(() -> auth,
					context);

			assertThat(result.isGranted()).isTrue();
		}

		@Test
		void hasNoAccess() {
			final var session = TestSession.ALISON_DACH_GLOVER;
			when(auth.getPrincipal()).thenReturn(session.principal());

			final AuthorizationResult result = manager.authorize(() -> auth,
					context);

			assertThat(result.isGranted()).isFalse();
		}
	}

	private class TestWebSocketAuthManager extends WebSocketAuthManager {

		public TestWebSocketAuthManager() {
			super(PATTERN);
		}

		@Override
		public boolean hasAccess(Principal principal,
				MessageAuthorizationContext<?> context) {
			return principal.userId() == TestUser.EDUARDO_CHRISTIANSEN.getId();
		}
	}
}
