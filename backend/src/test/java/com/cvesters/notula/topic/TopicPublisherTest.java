package com.cvesters.notula.topic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.common.domain.Origin;
import com.cvesters.notula.common.dto.OriginDto;
import com.cvesters.notula.common.messaging.TransactionalPublisher;
import com.cvesters.notula.session.TestSession;
import com.cvesters.notula.topic.bdo.TopicAction;
import com.cvesters.notula.topic.bdo.TopicEvent;
import com.cvesters.notula.topic.bdo.TopicInfo;
import com.cvesters.notula.topic.dto.TopicEventDto;
import com.cvesters.notula.topic.dto.TopicMutationDto;

class TopicPublisherTest {

	private static final String DESTINATION_PREFIX = "/topic/meetings";

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
	private static final UUID CLIENT_ID = UUID
			.fromString("3f9c1a44-1d2e-4a51-8b0c-2c7e9b1d4a0a");
	private static final Origin ORIGIN = new Origin(SESSION.principal(),
			CLIENT_ID);

	private final TransactionalPublisher publisher = mock();
	private final TopicPublisher topicPublisher = new TopicPublisher(
			publisher);

	@Nested
	class Publish {

		private static final long MEETING_ID = 1L;
		private static final long TOPIC_ID = 32L;

		private static final String DESTINATION = DESTINATION_PREFIX + "/"
				+ MEETING_ID;

		private final TopicInfo topic = mock();

		@BeforeEach
		void topic() {
			when(topic.getId()).thenReturn(TOPIC_ID);
			when(topic.getMeetingId()).thenReturn(MEETING_ID);
		}

		@Test
		void create() {
			final var action = new TopicAction.Create(MEETING_ID, 3, "New");
			final var event = new TopicEvent(topic, action, ORIGIN);

			topicPublisher.publish(event);

			verify(publisher).send(eq(DESTINATION),
					argThat((TopicEventDto dto) -> {
						assertThat(dto.getTopicId()).isEqualTo(TOPIC_ID);
						assertThat(dto.getOrigin())
								.isEqualTo(new OriginDto(ORIGIN));
						assertThat(dto.getMutation())
								.isInstanceOf(TopicMutationDto.Create.class);

						final var mutation = (TopicMutationDto.Create) dto
								.getMutation();
						assertThat(mutation.getSequenceId()).isEqualTo(3);
						assertThat(mutation.getName()).isEqualTo("New");
						return true;
					}));
		}

		@Test
		void updateName() {
			final var action = new TopicAction.UpdateName(4, 12, "Updated");
			final var event = new TopicEvent(topic, action, ORIGIN);

			topicPublisher.publish(event);

			verify(publisher).send(eq(DESTINATION),
					argThat((TopicEventDto dto) -> {
						assertThat(dto.getTopicId()).isEqualTo(TOPIC_ID);
						assertThat(dto.getOrigin())
								.isEqualTo(new OriginDto(ORIGIN));
						assertThat(dto.getMutation()).isInstanceOf(
								TopicMutationDto.UpdateName.class);

						final var mutation = (TopicMutationDto.UpdateName) dto
								.getMutation();
						assertThat(mutation.getPosition()).isEqualTo(4);
						assertThat(mutation.getLength()).isEqualTo(12);
						assertThat(mutation.getValue()).isEqualTo("Updated");
						return true;
					}));
		}

		@Test
		void updateDescription() {
			final var action = new TopicAction.UpdateDescription(4, 12,
					"Updated");
			final var event = new TopicEvent(topic, action, ORIGIN);

			topicPublisher.publish(event);

			verify(publisher).send(eq(DESTINATION),
					argThat((TopicEventDto dto) -> {
						assertThat(dto.getTopicId()).isEqualTo(TOPIC_ID);
						assertThat(dto.getOrigin())
								.isEqualTo(new OriginDto(ORIGIN));
						assertThat(dto.getMutation()).isInstanceOf(
								TopicMutationDto.UpdateDescription.class);

						final var mutation = (TopicMutationDto.UpdateDescription) dto
								.getMutation();
						assertThat(mutation.getPosition()).isEqualTo(4);
						assertThat(mutation.getLength()).isEqualTo(12);
						assertThat(mutation.getValue()).isEqualTo("Updated");
						return true;
					}));
		}

		@Test
		void delete() {
			final var action = new TopicAction.Delete();
			final var event = new TopicEvent(topic, action, ORIGIN);

			topicPublisher.publish(event);

			verify(publisher).send(eq(DESTINATION),
					argThat((TopicEventDto dto) -> {
						assertThat(dto.getTopicId()).isEqualTo(TOPIC_ID);
						assertThat(dto.getOrigin())
								.isEqualTo(new OriginDto(ORIGIN));
						assertThat(dto.getMutation())
								.isInstanceOf(TopicMutationDto.Delete.class);
						return true;
					}));

		}

		@Test
		void eventNull() {
			assertThatThrownBy(() -> topicPublisher.publish(null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
