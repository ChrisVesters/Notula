package com.cvesters.notula.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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

	
	@Nested
	class Constructor {

		@Test
		void patternNull() {
			assertThatThrownBy(() -> new TestWebSocketAuthManager(null))
					.isInstanceOf(NullPointerException.class);
		}

		@ParameterizedTest
		@ValueSource(strings = { "", " " })
		void patternInvalid(final String pattern) {
			assertThatThrownBy(() -> new TestWebSocketAuthManager(pattern))
					.isInstanceOf(IllegalArgumentException.class);
		}
		
	}
	
	@Nested
	class Authorize {
		private final WebSocketAuthManager manager = new TestWebSocketAuthManager();

		@Test
		void authenticationSupplierNull() {
			assertThatThrownBy(() -> manager.authorize(null, context))
					.isInstanceOf(NullPointerException.class);
		}

		@Test
		void contextNull() {
			assertThatThrownBy(() -> manager.authorize(() -> auth, null))
					.isInstanceOf(NullPointerException.class);
		}

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
			this(PATTERN);
		}

		public TestWebSocketAuthManager(final String pattern) {
			super(pattern);
		}

		@Override
		public boolean hasAccess(Principal principal,
				MessageAuthorizationContext<?> context) {
			return principal.userId() == TestUser.EDUARDO_CHRISTIANSEN.getId();
		}
	}
}
