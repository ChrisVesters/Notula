package com.cvesters.notula.common.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.cvesters.notula.common.domain.Principal;

class BaseControllerTest {

	private final BaseController controller = new BaseController() {
	};

	@Nested
	class GetPrincipal {

		private final Authentication authentication = mock();
		private final SecurityContext securityContext = mock();

		@BeforeEach
		void setup() {
			SecurityContextHolder.clearContext();
			SecurityContextHolder.setContext(securityContext);

			when(securityContext.getAuthentication())
					.thenReturn(authentication);
		}

		@AfterEach
		void teardown() {
			SecurityContextHolder.clearContext();
		}

		@Test
		void success() {
			final Jwt jwt = mock();
			when(jwt.getSubject()).thenReturn("12345");
			when(authentication.getPrincipal()).thenReturn(jwt);

			final Principal principal = controller.getPrincipal();

			assertThat(principal.userId()).isEqualTo(12345L);
		}

		@Test
		void principalInvalid() {
			final Object jwt = mock();
			when(authentication.getPrincipal()).thenReturn(jwt);

			assertThatThrownBy(() -> controller.getPrincipal())
					.isInstanceOf(IllegalStateException.class)
					.hasMessageContaining(
							"Unsupported Authentication principal type");
		}

		@Test
		void authenticationNull() {
			when(securityContext.getAuthentication()).thenReturn(null);

			assertThatThrownBy(() -> controller.getPrincipal())
					.isInstanceOf(IllegalStateException.class)
					.hasMessageContaining("User not authenticated");
		}

		@Test
		void noSubject() {
			final Jwt jwt = mock();
			when(authentication.getPrincipal()).thenReturn(jwt);

			assertThatThrownBy(() -> controller.getPrincipal())
					.isInstanceOf(NumberFormatException.class);
		}
	}

	@Nested
	class GetLocation {

		private static final String SCHEME = "http";
		private static final String SERVER_NAME = "localhost";
		private static final int SERVER_PORT = 8080;
		private static final String REQUEST_URI = "/api/resource";
		private static final String BASE_URL = SCHEME + "://" + SERVER_NAME
				+ ":" + SERVER_PORT + REQUEST_URI;

		private final ServletRequestAttributes attributes = mock();
		private final HttpServletRequest request = mock();

		@BeforeEach
		void setup() {
			when(attributes.getRequest()).thenReturn(request);
			when(request.getScheme()).thenReturn(SCHEME);
			when(request.getServerName()).thenReturn(SERVER_NAME);
			when(request.getServerPort()).thenReturn(SERVER_PORT);
			when(request.getRequestURI()).thenReturn(REQUEST_URI);

			RequestContextHolder.setRequestAttributes(attributes);
		}

		@AfterEach
		void teardown() {
			RequestContextHolder.resetRequestAttributes();
		}

		@Test
		void success() {
			final var uri = controller.getLocation("/{id}", 42);

			assertThat(uri.toString()).isEqualTo(BASE_URL + "/42");
		}

		@Test
		void pathNull() {
			assertThatThrownBy(() -> controller.getLocation(null))
					.isInstanceOf(NullPointerException.class);
		}

		@Test
		void uriVariablesNull() {
			assertThatThrownBy(() -> controller.getLocation("/{id}}", null))
					.isInstanceOf(NullPointerException.class);
		}

		@Test
		void uriVariablesNotEnough() {
			assertThatThrownBy(() -> controller.getLocation("/{id}}"))
					.isInstanceOf(IllegalArgumentException.class);
		}

		@Test
		void uriVariablesTooMany() {
			final var uri = controller.getLocation("/{id}", 42, 7);

			assertThat(uri.toString()).isEqualTo(BASE_URL + "/42");
		}
	}
}
