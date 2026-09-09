package com.cvesters.notula.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.security.crypto.password.PasswordEncoder;

class ConfigTest {

	private final Config config = new Config();

	@Nested
	class Encoder {

		@Test
		void success() {
			final PasswordEncoder encoder = config.passwordEncoder();

			final String encoded = encoder.encode("password");

			assertThat(encoded).isNotEqualTo("password");
			assertThat(encoder.matches("password", encoded)).isTrue();
			assertThat(encoder.matches("other", encoded)).isFalse();
		}

		@Test
		void salted() {
			final PasswordEncoder encoder = config.passwordEncoder();

			assertThat(encoder.encode("password"))
					.isNotEqualTo(encoder.encode("password"));
		}
	}

	@Nested
	class WebSocketInboundExecutor {

		@Test
		void success() {
			final var executor = config.webSocketInboundExecutor();

			assertThat(executor.getCorePoolSize())
					.isEqualTo(Runtime.getRuntime().availableProcessors());
			assertThat(executor.getThreadNamePrefix()).isEqualTo("ws-inbound-");
		}
	}

	@Nested
	class WebSocketTaskScheduler {

		@Test
		void success() {
			final var scheduler = (ThreadPoolTaskScheduler) config
					.webSocketTaskScheduler();

			try {
				assertThat(scheduler.getScheduledThreadPoolExecutor()
						.getCorePoolSize()).isEqualTo(2);
				assertThat(scheduler.getThreadNamePrefix())
						.isEqualTo("ws-heartbeat-");
				assertThat(scheduler.getScheduledExecutor()).isNotNull();
			} finally {
				scheduler.shutdown();
			}
		}
	}
}
