package com.cvesters.notula.event.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.common.domain.Origin;
import com.cvesters.notula.common.dto.OriginDto;
import com.cvesters.notula.event.bdo.EventInfo;
import com.cvesters.notula.event.bdo.TopicMutation;
import com.cvesters.notula.meeting.bdo.MeetingScope;
import com.cvesters.notula.session.TestSession;

import tools.jackson.databind.ObjectMapper;

class EventDtoTest {

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
	private static final UUID CLIENT_ID = UUID
			.fromString("3f9c1a44-1d2e-4a51-8b0c-2c7e9b1d4a06");
	private static final OriginDto ORIGIN = new OriginDto(
			new Origin(SESSION.principal(), CLIENT_ID));
	private static final long REVISION = 12;

	@Nested
	class Constructor {

		@Test
		void originNull() {
			final var mutation = new MeetingMutationDto.Remove();

			assertThatThrownBy(() -> new EventDto(REVISION, null, mutation))
					.isInstanceOf(NullPointerException.class);
		}

		@Test
		void mutationNull() {
			assertThatThrownBy(() -> new EventDto(REVISION, ORIGIN, null))
					.isInstanceOf(NullPointerException.class);
		}

		@Test
		void event() {
			final var origin = new Origin(SESSION.principal(), CLIENT_ID);
			final var scope = new MeetingScope(1L, REVISION);
			final var event = new EventInfo(scope, origin,
					new TopicMutation.Remove(32L));

			final var dto = new EventDto(event);

			assertThat(dto).isEqualTo(new EventDto(REVISION, ORIGIN,
					new TopicMutationDto.Remove(32L)));
		}

		@Test
		void eventNull() {
			assertThatThrownBy(() -> new EventDto(null))
					.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class Serialise {

		private static final ObjectMapper MAPPER = new ObjectMapper();

		@Test
		void success() {
			final var event = new EventDto(REVISION, ORIGIN,
					new TopicMutationDto.Remove(32L));

			final String json = MAPPER.writeValueAsString(event);

			assertThat(json).isEqualToIgnoringWhitespace("""
					{
						"revision": 12,
						"origin": {
							"userId": 1,
							"clientId": "3f9c1a44-1d2e-4a51-8b0c-2c7e9b1d4a06"
						},
						"mutation": {
							"type": "REMOVE_TOPIC",
							"topic": 32
						}
					}
					""");
		}

		@Test
		void withoutClientId() {
			final var origin = new OriginDto(new Origin(SESSION.principal()));
			final var event = new EventDto(REVISION, origin,
					new TopicMutationDto.Remove(32L));

			final String json = MAPPER.writeValueAsString(event);

			assertThat(json).isEqualToIgnoringWhitespace("""
					{
						"revision": 12,
						"origin": {
							"userId": 1,
							"clientId": null
						},
						"mutation": {
							"type": "REMOVE_TOPIC",
							"topic": 32
						}
					}
					""");
		}
	}
}
