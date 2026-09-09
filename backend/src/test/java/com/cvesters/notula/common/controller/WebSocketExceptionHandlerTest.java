package com.cvesters.notula.common.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.common.domain.ChangeId;
import com.cvesters.notula.common.dto.RejectedDto;
import com.cvesters.notula.common.exception.BusyEntityException;

class WebSocketExceptionHandlerTest {

	private static final UUID ID = UUID
			.fromString("7c6f0d54-2f70-4a1e-9f5a-1d4c8b2e0a11");

	private static final ChangeId CHANGE = new ChangeId(ID);

	private final WebSocketExceptionHandler handler =
			new WebSocketExceptionHandler();

	@Nested
	class HandleMissing {

		@Test
		void success() {
			final RejectedDto rejected = handler.handleMissing(CHANGE);

			assertThat(rejected.id()).isEqualTo(ID);
			assertThat(rejected.retryable()).isFalse();
			assertThat(rejected.reason()).isNotBlank();
		}

		@Test
		void withoutChange() {
			final RejectedDto rejected = handler.handleMissing(ChangeId.NONE);

			assertThat(rejected.id()).isNull();
			assertThat(rejected.reason()).isNotBlank();
		}
	}

	@Nested
	class HandleBusy {

		@Test
		void success() {
			final RejectedDto rejected = handler.handleBusy(CHANGE);

			assertThat(rejected.id()).isEqualTo(ID);
			assertThat(rejected.retryable()).isTrue();
			assertThat(rejected.reason()).isNotBlank();
		}

		@Test
		void withoutChange() {
			final RejectedDto rejected = handler.handleBusy(ChangeId.NONE);

			assertThat(rejected.id()).isNull();
			assertThat(rejected.retryable()).isTrue();
		}
	}

	@Nested
	class HandleInvalid {

		@Test
		void success() {
			final RejectedDto rejected = handler.handleInvalid(CHANGE);

			assertThat(rejected.id()).isEqualTo(ID);
			assertThat(rejected.retryable()).isFalse();
			assertThat(rejected.reason()).isNotBlank();
		}

		@Test
		void withoutChange() {
			final RejectedDto rejected = handler.handleInvalid(ChangeId.NONE);

			assertThat(rejected.id()).isNull();
			assertThat(rejected.reason()).isNotBlank();
		}
	}

	@Nested
	class Handle {

		@Test
		void success() {
			final var exception = new BusyEntityException("Locked");

			final RejectedDto rejected = handler.handle(CHANGE, exception);

			assertThat(rejected.id()).isEqualTo(ID);
			assertThat(rejected.retryable()).isFalse();
			assertThat(rejected.reason()).isNotBlank();
		}

		@Test
		void message() {
			final var exception = new IllegalStateException(
					"jdbc:postgresql://user:secret@db/notula");

			final RejectedDto rejected = handler.handle(CHANGE, exception);

			assertThat(rejected.reason()).doesNotContain("secret");
		}
	}
}
