package com.cvesters.notula.meeting;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.block.bdo.BlockAction;
import com.cvesters.notula.block.bdo.BlockEvent;
import com.cvesters.notula.block.bdo.BlockInfo;
import com.cvesters.notula.block.bdo.BlockType;
import com.cvesters.notula.block.dto.BlockTypeDto;
import com.cvesters.notula.common.domain.Origin;
import com.cvesters.notula.common.dto.OriginDto;
import com.cvesters.notula.common.messaging.TransactionalPublisher;
import com.cvesters.notula.meeting.bdo.MeetingAction;
import com.cvesters.notula.meeting.bdo.MeetingEvent;
import com.cvesters.notula.meeting.bdo.MeetingScope;
import com.cvesters.notula.meeting.dto.BlockMutationDto;
import com.cvesters.notula.meeting.dto.EventDto;
import com.cvesters.notula.meeting.dto.MeetingMutationDto;
import com.cvesters.notula.meeting.dto.MutationDto;
import com.cvesters.notula.meeting.dto.TextBlockMutationDto;
import com.cvesters.notula.meeting.dto.TextEditDto;
import com.cvesters.notula.meeting.dto.TopicMutationDto;
import com.cvesters.notula.session.TestSession;
import com.cvesters.notula.textblock.bdo.TextBlockAction;
import com.cvesters.notula.textblock.bdo.TextBlockEvent;
import com.cvesters.notula.topic.bdo.TopicAction;
import com.cvesters.notula.topic.bdo.TopicEvent;
import com.cvesters.notula.topic.bdo.TopicInfo;

class EventPublisherTest {

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
	private static final UUID CLIENT_ID = UUID
			.fromString("3f9c1a44-1d2e-4a51-8b0c-2c7e9b1d4a0b");
	private static final Origin ORIGIN = new Origin(SESSION.principal(),
			CLIENT_ID);

	private static final long MEETING_ID = 1L;
	private static final long REVISION = 12L;
	private static final MeetingScope SCOPE = new MeetingScope(MEETING_ID,
			REVISION);
	private static final long TOPIC_ID = 32L;
	private static final long BLOCK_ID = 61L;

	private static final String DESTINATION = "/topic/meetings/" + MEETING_ID;

	private final TransactionalPublisher publisher = mock();
	private final EventPublisher eventPublisher = new EventPublisher(
			publisher);

	private void verifySent(final MutationDto mutation) {
		final var expected = new EventDto(REVISION, new OriginDto(ORIGIN),
				mutation);

		verify(publisher).send(DESTINATION, expected);
	}

	@Nested
	class Publish {

		@Nested
		class Meeting {

			@Test
			void create() {
				final var action = new MeetingAction.Create("New");
				final var event = new MeetingEvent(action, ORIGIN);

				eventPublisher.publish(SCOPE, event);

				verifySent(new MeetingMutationDto.Add("New"));
			}

			@Test
			void updateName() {
				final var action = new MeetingAction.UpdateName(4, 12,
						"Updated");
				final var event = new MeetingEvent(action, ORIGIN);

				eventPublisher.publish(SCOPE, event);

				final var edit = new TextEditDto(4, 12, "Updated");
				verifySent(new MeetingMutationDto.Rename(edit));
			}

			@Test
			void updateDescription() {
				final var action = new MeetingAction.UpdateDescription(4, 12,
						"Updated");
				final var event = new MeetingEvent(action, ORIGIN);

				eventPublisher.publish(SCOPE, event);

				final var edit = new TextEditDto(4, 12, "Updated");
				verifySent(new MeetingMutationDto.Describe(edit));
			}

			@Test
			void delete() {
				final var action = new MeetingAction.Delete();
				final var event = new MeetingEvent(action, ORIGIN);

				eventPublisher.publish(SCOPE, event);

				verifySent(new MeetingMutationDto.Remove());
			}

			@Test
			void eventNull() {
				assertThatThrownBy(() -> eventPublisher.publish(SCOPE,
						(MeetingEvent) null))
						.isInstanceOf(NullPointerException.class);
			}
		}

		@Nested
		class Topic {

			private final TopicInfo topic = mock();

			@BeforeEach
			void topic() {
				when(topic.getId()).thenReturn(TOPIC_ID);
			}

			@Test
			void create() {
				final var action = new TopicAction.Create(3, "New");
				final var event = new TopicEvent(topic, action, ORIGIN);

				eventPublisher.publish(SCOPE, event);

				verifySent(new TopicMutationDto.Add(TOPIC_ID, 3, "New"));
			}

			@Test
			void move() {
				final var action = new TopicAction.Move(3);
				final var event = new TopicEvent(topic, action, ORIGIN);

				eventPublisher.publish(SCOPE, event);

				verifySent(new TopicMutationDto.Move(TOPIC_ID, 3));
			}

			@Test
			void updateName() {
				final var action = new TopicAction.UpdateName(4, 12,
						"Updated");
				final var event = new TopicEvent(topic, action, ORIGIN);

				eventPublisher.publish(SCOPE, event);

				final var edit = new TextEditDto(4, 12, "Updated");
				verifySent(new TopicMutationDto.Rename(TOPIC_ID, edit));
			}

			@Test
			void updateDescription() {
				final var action = new TopicAction.UpdateDescription(4, 12,
						"Updated");
				final var event = new TopicEvent(topic, action, ORIGIN);

				eventPublisher.publish(SCOPE, event);

				final var edit = new TextEditDto(4, 12, "Updated");
				verifySent(new TopicMutationDto.Describe(TOPIC_ID, edit));
			}

			@Test
			void delete() {
				final var action = new TopicAction.Delete();
				final var event = new TopicEvent(topic, action, ORIGIN);

				eventPublisher.publish(SCOPE, event);

				verifySent(new TopicMutationDto.Remove(TOPIC_ID));
			}

			@Test
			void eventNull() {
				assertThatThrownBy(() -> eventPublisher.publish(SCOPE,
						(TopicEvent) null))
						.isInstanceOf(NullPointerException.class);
			}
		}

		@Nested
		class Block {

			private final BlockInfo block = mock();

			@BeforeEach
			void block() {
				when(block.getId()).thenReturn(BLOCK_ID);
			}

			@Test
			void create() {
				final var action = new BlockAction.Create(TOPIC_ID,
						BlockType.TEXT, 3);
				final var event = new BlockEvent(block, action, ORIGIN);

				eventPublisher.publish(SCOPE, event);

				verifySent(new BlockMutationDto.Add(BLOCK_ID, TOPIC_ID,
						new BlockTypeDto(BlockType.TEXT), 3));
			}

			@Test
			void move() {
				final var action = new BlockAction.Move(3);
				final var event = new BlockEvent(block, action, ORIGIN);

				eventPublisher.publish(SCOPE, event);

				verifySent(new BlockMutationDto.Move(BLOCK_ID, 3));
			}

			@Test
			void delete() {
				final var action = new BlockAction.Delete();
				final var event = new BlockEvent(block, action, ORIGIN);

				eventPublisher.publish(SCOPE, event);

				verifySent(new BlockMutationDto.Remove(BLOCK_ID));
			}

			@Test
			void eventNull() {
				assertThatThrownBy(() -> eventPublisher.publish(SCOPE,
						(BlockEvent) null))
						.isInstanceOf(NullPointerException.class);
			}
		}

		@Nested
		class TextBlock {

			private final BlockInfo block = mock();

			@BeforeEach
			void block() {
				when(block.getId()).thenReturn(BLOCK_ID);
			}

			@Test
			void updateContent() {
				final var action = new TextBlockAction.UpdateContent(2, 3,
						"New");
				final var event = new TextBlockEvent(block, action, ORIGIN);

				eventPublisher.publish(SCOPE, event);

				final var edit = new TextEditDto(2, 3, "New");
				verifySent(new TextBlockMutationDto.Edit(BLOCK_ID, edit));
			}

			@Test
			void eventNull() {
				assertThatThrownBy(() -> eventPublisher.publish(SCOPE,
						(TextBlockEvent) null))
						.isInstanceOf(NullPointerException.class);
			}
		}
	}
}
