package com.cvesters.notula.common.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.session.TestSession;
import com.cvesters.notula.user.TestUser;

class OriginTest {

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;

	private static final UUID CLIENT_ID = UUID
			.fromString("0e2ea0cd-3d1c-4a3f-9f11-3e5c1e2c4a77");

	@Nested
	class Constructor {

		@Test
		void success() {
			final Principal principal = SESSION.principal();

			final var origin = new Origin(principal, CLIENT_ID);

			assertThat(origin.principal()).isEqualTo(principal);
			assertThat(origin.clientId()).isEqualTo(CLIENT_ID);
		}

		@Test
		void principalOnly() {
			final Principal principal = SESSION.principal();

			final var origin = new Origin(principal);

			assertThat(origin.principal()).isEqualTo(principal);
			assertThat(origin.clientId()).isNull();
		}

		@Test
		void clientIdNull() {
			final Principal principal = SESSION.principal();

			final var origin = new Origin(principal, null);

			assertThat(origin.principal()).isEqualTo(principal);
			assertThat(origin.clientId()).isNull();
		}

		@Test
		void principalNull() {
			assertThatThrownBy(() -> new Origin(null, CLIENT_ID))
					.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class UserId {

		@Test
		void success() {
			final TestUser user = SESSION.getUser();
			final var origin = new Origin(SESSION.principal(), CLIENT_ID);

			assertThat(origin.userId()).isEqualTo(user.getId());
		}

		@Test
		void withoutClientId() {
			final TestUser user = SESSION.getUser();
			final var origin = new Origin(SESSION.principal());

			assertThat(origin.userId()).isEqualTo(user.getId());
		}

		@Test
		void withoutOrganisation() {
			final TestSession session = TestSession.KRISTINA_THIEL;
			final TestUser user = session.getUser();
			final var origin = new Origin(session.principal(), CLIENT_ID);

			assertThat(origin.userId()).isEqualTo(user.getId());
		}
	}
}
