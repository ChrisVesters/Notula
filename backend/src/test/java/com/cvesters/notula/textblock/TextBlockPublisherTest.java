package com.cvesters.notula.textblock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.cvesters.notula.textblock.bdo.TextBlockAction;
import com.cvesters.notula.textblock.dao.TextBlockEvent;
import com.cvesters.notula.textblock.dto.TextBlockEventDto;
import com.cvesters.notula.textblock.dto.TextBlockMutationDto;

class TextBlockPublisherTest {

	private static final String DESTINATION_PREFIX = "/topic/meetings";

	private final SimpMessagingTemplate messagingTemplate = mock();
	private final TextBlockPublisher publisher = new TextBlockPublisher(
			messagingTemplate);

	@Nested
	class Publish {

		private static final long MEETING_ID = 1L;
		private static final long TOPIC_ID = 32L;
		private static final long BLOCK_ID = 61L;

		private static final String DESTINATION = DESTINATION_PREFIX + "/"
				+ MEETING_ID;

		@Test
		void updateContent() {
			final var action = new TextBlockAction.UpdateContent(2, 3, "New");
			final var event = new TextBlockEvent(TOPIC_ID, BLOCK_ID,
					action);

			publisher.publish(MEETING_ID, event);

			verify(messagingTemplate).convertAndSend(eq(DESTINATION),
					argThat((TextBlockEventDto dto) -> {
						assertThat(dto.getTopicId()).isEqualTo(TOPIC_ID);
						assertThat(dto.getBlockId()).isEqualTo(BLOCK_ID);
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
		void eventNull() {
			assertThatThrownBy(() -> publisher.publish(MEETING_ID, null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
