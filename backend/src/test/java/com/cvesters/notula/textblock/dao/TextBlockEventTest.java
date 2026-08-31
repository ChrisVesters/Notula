package com.cvesters.notula.textblock.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.util.UUID;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.block.bdo.BlockInfo;
import com.cvesters.notula.common.domain.Origin;
import com.cvesters.notula.session.TestSession;
import com.cvesters.notula.textblock.bdo.TextBlockAction;

class TextBlockEventTest {

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
	private static final UUID CLIENT_ID = UUID
			.fromString("3f9c1a44-1d2e-4a51-8b0c-2c7e9b1d4a04");
	private static final Origin ORIGIN = new Origin(SESSION.principal(),
			CLIENT_ID);

	@Nested
	class Constructor {

		@Test
		void success() {
			final BlockInfo block = mock();
			final var action = new TextBlockAction.UpdateContent(0, 0, "New");
			final var event = new TextBlockEvent(block, action, ORIGIN);

			assertThat(event.block()).isEqualTo(block);
			assertThat(event.action()).isEqualTo(action);
			assertThat(event.origin()).isEqualTo(ORIGIN);
		}

		@Test
		void blockNull() {
			final var action = new TextBlockAction.UpdateContent(0, 0, "New");

			assertThatThrownBy(() -> new TextBlockEvent(null, action, ORIGIN))
					.isInstanceOf(NullPointerException.class);
		}

		@Test
		void actionNull() {
			final BlockInfo block = mock();

			assertThatThrownBy(() -> new TextBlockEvent(block, null, ORIGIN))
					.isInstanceOf(NullPointerException.class);
		}

		@Test
		void originNull() {
			final BlockInfo block = mock();
			final var action = new TextBlockAction.UpdateContent(0, 0, "New");

			assertThatThrownBy(() -> new TextBlockEvent(block, action, null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
