package com.cvesters.notula.event.bdo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.common.domain.Origin;
import com.cvesters.notula.meeting.bdo.MeetingScope;
import com.cvesters.notula.session.TestSession;

class EventInfoTest {

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
	private static final UUID CLIENT_ID = UUID
			.fromString("3f9c1a44-1d2e-4a51-8b0c-2c7e9b1d4a20");
	private static final Origin ORIGIN = new Origin(SESSION.principal(),
			CLIENT_ID);
	private static final long USER_ID = SESSION.principal().userId();
	private static final UUID CHANGE_ID = UUID
			.fromString("7c6f0d54-2f70-4a1e-9f5a-1d4c8b2e0a11");

	private static final long ID = 7L;
	private static final long MEETING_ID = 1L;
	private static final long REVISION = 12L;
	private static final MeetingScope SCOPE = new MeetingScope(MEETING_ID,
			REVISION, CHANGE_ID);
	private static final TopicMutation MUTATION = new TopicMutation.Remove(
			32L);

	@Nested
	class Constructor {

		@Test
		void scope() {
			final var event = new EventInfo(SCOPE, ORIGIN, MUTATION);

			assertThatThrownBy(event::getId)
					.isInstanceOf(IllegalStateException.class);
			assertThat(event.getMeetingId()).isEqualTo(MEETING_ID);
			assertThat(event.getRevision()).isEqualTo(REVISION);
			assertThat(event.getUserId()).isEqualTo(USER_ID);
			assertThat(event.getClientId()).isEqualTo(CLIENT_ID);
			assertThat(event.getChangeId()).isEqualTo(CHANGE_ID);
			assertThat(event.getMutation()).isEqualTo(MUTATION);
		}

		@Test
		void withoutChangeId() {
			final var scope = new MeetingScope(MEETING_ID, REVISION);

			final var event = new EventInfo(scope, ORIGIN, MUTATION);

			assertThat(event.getChangeId()).isNull();
		}

		@Test
		void withoutClientId() {
			final var origin = new Origin(SESSION.principal());

			final var event = new EventInfo(SCOPE, origin, MUTATION);

			assertThat(event.getUserId()).isEqualTo(USER_ID);
			assertThat(event.getClientId()).isNull();
		}

		@Test
		void scopeNull() {
			assertThatThrownBy(() -> new EventInfo(null, ORIGIN, MUTATION))
					.isInstanceOf(NullPointerException.class);
		}

		@Test
		void originNull() {
			assertThatThrownBy(() -> new EventInfo(SCOPE, null, MUTATION))
					.isInstanceOf(NullPointerException.class);
		}

		@Test
		void full() {
			final var event = new EventInfo(ID, MEETING_ID, REVISION, USER_ID,
					CLIENT_ID, CHANGE_ID, MUTATION);

			assertThat(event.getId()).isEqualTo(ID);
			assertThat(event.getMeetingId()).isEqualTo(MEETING_ID);
			assertThat(event.getRevision()).isEqualTo(REVISION);
			assertThat(event.getUserId()).isEqualTo(USER_ID);
			assertThat(event.getClientId()).isEqualTo(CLIENT_ID);
			assertThat(event.getChangeId()).isEqualTo(CHANGE_ID);
			assertThat(event.getMutation()).isEqualTo(MUTATION);
		}

		@Test
		void mutationNull() {
			assertThatThrownBy(() -> new EventInfo(ID, MEETING_ID, REVISION,
					USER_ID, CLIENT_ID, CHANGE_ID, null))
							.isInstanceOf(NullPointerException.class);
		}
	}
}
