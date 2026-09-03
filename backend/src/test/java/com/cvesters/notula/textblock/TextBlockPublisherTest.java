package com.cvesters.notula.textblock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.block.bdo.BlockInfo;
import com.cvesters.notula.common.domain.Origin;
import com.cvesters.notula.common.dto.OriginDto;
import com.cvesters.notula.common.exception.MissingEntityException;
import com.cvesters.notula.common.messaging.TransactionalPublisher;
import com.cvesters.notula.session.TestSession;
import com.cvesters.notula.textblock.bdo.TextBlockAction;
import com.cvesters.notula.textblock.dao.TextBlockEvent;
import com.cvesters.notula.textblock.dto.TextBlockEventDto;
import com.cvesters.notula.textblock.dto.TextBlockMutationDto;
import com.cvesters.notula.topic.TopicStorageGateway;
import com.cvesters.notula.topic.bdo.TopicInfo;

class TextBlockPublisherTest {

	private static final String DESTINATION_PREFIX = "/topic/meetings";

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
	private static final UUID CLIENT_ID = UUID
			.fromString("3f9c1a44-1d2e-4a51-8b0c-2c7e9b1d4a0c");
	private static final Origin ORIGIN = new Origin(SESSION.principal(),
			CLIENT_ID);

	private final TransactionalPublisher publisher = mock();
	private final TopicStorageGateway topicStorage = mock();
	private final TextBlockPublisher textBlockPublisher =
			new TextBlockPublisher(publisher, topicStorage);

	@Nested
	class Publish {

		private static final long MEETING_ID = 1L;
		private static final long TOPIC_ID = 32L;
		private static final long BLOCK_ID = 61L;

		private static final String DESTINATION = DESTINATION_PREFIX + "/"
				+ MEETING_ID;

		private final BlockInfo block = mock();

		@BeforeEach
		void block() {
			when(block.getId()).thenReturn(BLOCK_ID);
			when(block.getTopicId()).thenReturn(TOPIC_ID);
		}

		@Test
		void updateContent() {
			final TopicInfo topic = mock();
			when(topic.getMeetingId()).thenReturn(MEETING_ID);
			when(topicStorage.find(TOPIC_ID)).thenReturn(Optional.of(topic));

			final var action = new TextBlockAction.UpdateContent(2, 3, "New");
			final var event = new TextBlockEvent(block, action, ORIGIN);

			textBlockPublisher.publish(event);

			verify(publisher).send(eq(DESTINATION),
					argThat((TextBlockEventDto dto) -> {
						assertThat(dto.getBlockId()).isEqualTo(BLOCK_ID);
						assertThat(dto.getOrigin())
								.isEqualTo(new OriginDto(ORIGIN));
						assertThat(dto.getMutation()).isInstanceOf(
								TextBlockMutationDto.UpdateContent.class);

						final var mutation = (TextBlockMutationDto.UpdateContent) dto
								.getMutation();
						assertThat(mutation.getPosition()).isEqualTo(2);
						assertThat(mutation.getLength()).isEqualTo(3);
						assertThat(mutation.getValue()).isEqualTo("New");
						return true;
					}));
		}

		@Test
		void topicNotFound() {
			final TopicInfo topic = mock();
			when(topic.getMeetingId()).thenReturn(MEETING_ID);
			when(topicStorage.find(TOPIC_ID)).thenReturn(Optional.of(topic));

			when(topicStorage.find(TOPIC_ID)).thenReturn(Optional.empty());

			final var action = new TextBlockAction.UpdateContent(2, 3, "New");
			final var event = new TextBlockEvent(block, action, ORIGIN);

			assertThatThrownBy(() -> textBlockPublisher.publish(event))
					.isInstanceOf(MissingEntityException.class);

			verifyNoInteractions(publisher);
		}

		@Test
		void eventNull() {
			assertThatThrownBy(() -> textBlockPublisher.publish(null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
