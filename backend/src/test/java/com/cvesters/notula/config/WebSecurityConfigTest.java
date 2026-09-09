package com.cvesters.notula.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;

class WebSecurityConfigTest {

	private static final String FRONTEND_URL = "https://localhost:4443";

	private final WebSecurityConfig config = new WebSecurityConfig(
			mock(JwtAuthConverter.class), FRONTEND_URL);

	@Nested
	class Cors {

		@Test
		void success() {
			final CorsConfiguration configuration = configuration("/api/users");

			assertThat(configuration).isNotNull();
			assertThat(configuration.getAllowedOrigins())
					.containsExactly(FRONTEND_URL);
			assertThat(configuration.getAllowedMethods()).containsExactly("*");
			assertThat(configuration.getAllowedHeaders()).containsExactly("*");
			assertThat(configuration.getAllowCredentials()).isTrue();
		}

		@Test
		void otherOrigin() {
			final CorsConfiguration configuration = configuration("/api/users");

			assertThat(configuration.checkOrigin("https://elsewhere.example"))
					.isNull();
			assertThat(configuration.checkOrigin(FRONTEND_URL))
					.isEqualTo(FRONTEND_URL);
		}

		private CorsConfiguration configuration(final String path) {
			final var request = new MockHttpServletRequest("GET", path);

			return config.corsConfigurationSource()
					.getCorsConfiguration(request);
		}
	}
}
