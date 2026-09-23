package com.cvesters.notula.block;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.cvesters.notula.block.bdo.BlockAction;
import com.cvesters.notula.block.bdo.BlockEvent;
import com.cvesters.notula.block.bdo.BlockInfo;
import com.cvesters.notula.block.bdo.BlockType;
import com.cvesters.notula.common.domain.Origin;
import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.common.exception.MissingEntityException;
import com.cvesters.notula.meeting.EventPublisher;
import com.cvesters.notula.meeting.TestMeeting;
import com.cvesters.notula.meeting.bdo.MeetingScope;
import com.cvesters.notula.organisation.TestOrganisation;
import com.cvesters.notula.session.TestSession;
import com.cvesters.notula.topic.TestTopic;
import com.cvesters.notula.topic.TopicService;

class BlockServiceTest {

	private static final UUID CLIENT_ID = UUID
			.fromString("3f9c1a44-1d2e-4a51-8b0c-2c7e9b1d4a0d");

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
	private static final Principal PRINCIPAL = SESSION.principal();
	private static final Origin ORIGIN = new Origin(PRINCIPAL, CLIENT_ID);

	private final TopicService topicService = mock();

	private final BlockStorageGateway blockStorageGateway = mock();
	private final EventPublisher eventPublisher = mock();

	private final BlockService blockService = new BlockService(topicService,
			blockStorageGateway, eventPublisher);

	@Nested
	class GetById {

		private static final TestBlock BLOCK = TestBlock.SPORER_PROJECT_BLOCKERS_FIRST;

		@Test
		void success() {
			final BlockInfo blockInfo = BLOCK.info();
			when(blockStorageGateway.find(BLOCK.getId()))
					.thenReturn(Optional.of(blockInfo));

			final BlockInfo result = blockService.getById(PRINCIPAL,
					BLOCK.getId());

			assertThat(result).isEqualTo(blockInfo);
		}

		@Test
		void notFound() {
			when(blockStorageGateway.find(BLOCK.getId()))
					.thenReturn(Optional.empty());

			final long blockId = BLOCK.getId();

			assertThatThrownBy(() -> blockService.getById(PRINCIPAL, blockId))
					.isInstanceOf(MissingEntityException.class);
		}

		@Test
		void otherOrganisation() {
			final Principal principal = TestSession.ALISON_DACH_GLOVER
					.principal();

			final BlockInfo blockInfo = BLOCK.info();
			when(blockStorageGateway.find(BLOCK.getId()))
					.thenReturn(Optional.of(blockInfo));

			final long blockId = BLOCK.getId();

			assertThatThrownBy(() -> blockService.getById(principal, blockId))
					.isInstanceOf(MissingEntityException.class);
		}

		@Test
		void principalNull() {
			final long blockId = BLOCK.getId();

			assertThatThrownBy(() -> blockService.getById(null, blockId))
					.isInstanceOf(NullPointerException.class);
		}

		@Test
		void inMeeting() {
			final TestTopic topic = BLOCK.getTopic();
			final long meetingId = topic.getMeeting().getId();

			final BlockInfo blockInfo = BLOCK.info();
			when(blockStorageGateway.find(BLOCK.getId()))
					.thenReturn(Optional.of(blockInfo));

			final BlockInfo result = blockService.getById(PRINCIPAL, meetingId,
					BLOCK.getId());

			assertThat(result).isEqualTo(blockInfo);

			verify(topicService).getById(PRINCIPAL, meetingId, topic.getId());
		}

		@Test
		void otherMeeting() {
			final TestTopic topic = BLOCK.getTopic();
			final long meetingId = TestMeeting.SPORER_RETRO.getId();
			final long blockId = BLOCK.getId();

			when(blockStorageGateway.find(blockId))
					.thenReturn(Optional.of(BLOCK.info()));
			when(topicService.getById(PRINCIPAL, meetingId, topic.getId()))
					.thenThrow(new MissingEntityException());

			assertThatThrownBy(
					() -> blockService.getById(PRINCIPAL, meetingId, blockId))
							.isInstanceOf(MissingEntityException.class);
		}
	}

	@Nested
	class Create {

		private static final TestBlock BLOCK = TestBlock.SPORER_PROJECT_BLOCKERS_FIRST;
		private static final TestTopic TOPIC = BLOCK.getTopic();
		private static final TestMeeting MEETING = TOPIC.getMeeting();
		private static final TestOrganisation ORGANISATION = MEETING
				.getOrganisation();
		private static final long MEETING_ID = MEETING.getId();

		private static final long REVISION = MEETING.getRevision();
		private static final MeetingScope SCOPE = new MeetingScope(MEETING_ID,
				REVISION);

		private final List<BlockInfo> siblings = TestBlock.ofTopic(TOPIC)
				.stream()
				.map(TestBlock::info)
				.toList();

		@BeforeEach
		void setup() {
			when(topicService.getById(PRINCIPAL, MEETING_ID, TOPIC.getId()))
					.thenReturn(TOPIC.info());
			when(blockStorageGateway.findAllByTopicId(TOPIC.getId()))
					.thenReturn(siblings);
			when(blockStorageGateway.create(any()))
					.thenAnswer(invocation -> invocation.getArgument(0));
		}

		@Test
		void between() {
			final BlockInfo after = siblings.get(0);
			final BlockInfo next = siblings.get(1);

			final var action = new BlockAction.Create(TOPIC.getId(),
					BlockType.TEXT, after.getId());

			final BlockInfo created = blockService.create(ORIGIN, SCOPE,
					action);

			assertThat(created.getRank()).isGreaterThan(after.getRank())
					.isLessThan(next.getRank());
			assertThat(created.getOrganisationId())
					.isEqualTo(ORGANISATION.getId());
			assertThat(created.getTopicId()).isEqualTo(TOPIC.getId());
			assertThat(created.getType()).isEqualTo(BlockType.TEXT);

			verify(blockStorageGateway).create(created);
			verify(blockStorageGateway, never()).update(any());
			verify(eventPublisher).publish(SCOPE,
					new BlockEvent(created, action, ORIGIN));
			verifyNoMoreInteractions(eventPublisher);
		}

		@Test
		void first() {
			final var action = new BlockAction.Create(TOPIC.getId(),
					BlockType.TEXT, null);

			final BlockInfo created = blockService.create(ORIGIN, SCOPE,
					action);

			assertThat(created.getRank())
					.isLessThan(siblings.getFirst().getRank());
			assertThat(created.getOrganisationId())
					.isEqualTo(ORGANISATION.getId());
			assertThat(created.getTopicId()).isEqualTo(TOPIC.getId());
			assertThat(created.getType()).isEqualTo(BlockType.TEXT);

			verify(blockStorageGateway).create(created);
			verify(blockStorageGateway, never()).update(any());
			verify(eventPublisher).publish(SCOPE,
					new BlockEvent(created, action, ORIGIN));
			verifyNoMoreInteractions(eventPublisher);
		}

		@Test
		void last() {
			final BlockInfo last = siblings.getLast();

			final var action = new BlockAction.Create(TOPIC.getId(),
					BlockType.TEXT, last.getId());

			final BlockInfo created = blockService.create(ORIGIN, SCOPE,
					action);

			assertThat(created.getRank()).isGreaterThan(last.getRank());
			assertThat(created.getOrganisationId())
					.isEqualTo(ORGANISATION.getId());
			assertThat(created.getTopicId()).isEqualTo(TOPIC.getId());
			assertThat(created.getType()).isEqualTo(BlockType.TEXT);

			verify(blockStorageGateway).create(created);
			verify(blockStorageGateway, never()).update(any());
			verify(eventPublisher).publish(SCOPE,
					new BlockEvent(created, action, ORIGIN));
			verifyNoMoreInteractions(eventPublisher);
		}

		@Test
		void only() {
			when(blockStorageGateway.findAllByTopicId(TOPIC.getId()))
					.thenReturn(List.of());

			final var action = new BlockAction.Create(TOPIC.getId(),
					BlockType.TEXT, null);

			final BlockInfo created = blockService.create(ORIGIN, SCOPE,
					action);

			assertThat(created.getRank()).isNotNull();
			assertThat(created.getOrganisationId())
					.isEqualTo(ORGANISATION.getId());
			assertThat(created.getTopicId()).isEqualTo(TOPIC.getId());
			assertThat(created.getType()).isEqualTo(BlockType.TEXT);

			verify(blockStorageGateway).create(created);
			verify(blockStorageGateway, never()).update(any());
			verify(eventPublisher).publish(SCOPE,
					new BlockEvent(created, action, ORIGIN));
			verifyNoMoreInteractions(eventPublisher);
		}

		@Test
		void afterIdInvalid() {
			final var action = new BlockAction.Create(TOPIC.getId(),
					BlockType.TEXT, Long.MAX_VALUE);

			assertThatThrownBy(() -> blockService.create(ORIGIN, SCOPE, action))
					.isInstanceOf(MissingEntityException.class);

			verify(blockStorageGateway, never()).create(any());
			verifyNoInteractions(eventPublisher);
		}

		@Test
		void originNull() {
			final var action = new BlockAction.Create(TOPIC.getId(),
					BlockType.TEXT, null);

			assertThatThrownBy(() -> blockService.create(null, SCOPE, action))
					.isInstanceOf(NullPointerException.class);
		}

		@Test
		void scopeNull() {
			final var action = new BlockAction.Create(TOPIC.getId(),
					BlockType.TEXT, null);

			assertThatThrownBy(() -> blockService.create(ORIGIN, null, action))
					.isInstanceOf(NullPointerException.class);
		}

		@Test
		void actionNull() {
			assertThatThrownBy(() -> blockService.create(ORIGIN, SCOPE, null))
					.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class Move {

		private static final TestBlock BLOCK = TestBlock.SPORER_PROJECT_BLOCKERS_THIRD;
		private static final TestTopic TOPIC = BLOCK.getTopic();
		private static final TestMeeting MEETING = TOPIC.getMeeting();
		private static final long MEETING_ID = MEETING.getId();

		private static final long REVISION = MEETING.getRevision();
		private static final MeetingScope SCOPE = new MeetingScope(MEETING_ID,
				REVISION);

		private final List<BlockInfo> siblings = TestBlock.ofTopic(TOPIC)
				.stream()
				.map(TestBlock::info)
				.toList();

		@BeforeEach
		void blocks() {
			when(blockStorageGateway.find(BLOCK.getId()))
					.thenReturn(Optional.of(BLOCK.info()));
			when(topicService.getById(PRINCIPAL, MEETING_ID, TOPIC.getId()))
					.thenReturn(TOPIC.info());
			when(blockStorageGateway.findAllByTopicId(TOPIC.getId()))
					.thenReturn(siblings);
			when(blockStorageGateway.update(any()))
					.thenAnswer(invocation -> invocation.getArgument(0));
		}

		@Test
		void between() {
			final BlockInfo after = siblings.get(0);
			final BlockInfo next = siblings.get(1);

			final var action = new BlockAction.Move(after.getId());

			final BlockInfo moved = blockService.move(ORIGIN, SCOPE,
					BLOCK.getId(), action);

			assertThat(moved.getId()).isEqualTo(BLOCK.getId());
			assertThat(moved.getRank()).isGreaterThan(after.getRank())
					.isLessThan(next.getRank());

			verify(blockStorageGateway).update(moved);
			verify(blockStorageGateway, never()).create(any());
			verify(eventPublisher).publish(SCOPE,
					new BlockEvent(moved, action, ORIGIN));
			verifyNoMoreInteractions(eventPublisher);
		}

		@Test
		void first() {
			final var action = new BlockAction.Move(null);

			final BlockInfo moved = blockService.move(ORIGIN, SCOPE,
					BLOCK.getId(), action);

			assertThat(moved.getRank())
					.isLessThan(siblings.getFirst().getRank());

			verify(blockStorageGateway).update(moved);
			verify(blockStorageGateway, never()).create(any());
			verify(eventPublisher).publish(SCOPE,
					new BlockEvent(moved, action, ORIGIN));
			verifyNoMoreInteractions(eventPublisher);
		}

		@Test
		void inPlace() {
			final BlockInfo above = siblings.get(siblings.size() - 2);

			final var action = new BlockAction.Move(above.getId());

			final BlockInfo moved = blockService.move(ORIGIN, SCOPE,
					BLOCK.getId(), action);

			assertThat(moved.getRank()).isGreaterThan(above.getRank());

			verify(blockStorageGateway).update(moved);
			verify(blockStorageGateway, never()).create(any());
			verify(eventPublisher).publish(SCOPE,
					new BlockEvent(moved, action, ORIGIN));
			verifyNoMoreInteractions(eventPublisher);
		}

		@Test
		void unknownNeighbour() {
			final var action = new BlockAction.Move(Long.MAX_VALUE);

			assertThatThrownBy(() -> blockService.move(ORIGIN, SCOPE,
					BLOCK.getId(), action))
							.isInstanceOf(MissingEntityException.class);

			verify(blockStorageGateway, never()).update(any());
			verifyNoInteractions(eventPublisher);
		}

		@Test
		void unknownBlock() {
			when(blockStorageGateway.find(anyLong()))
					.thenReturn(Optional.empty());

			final var action = new BlockAction.Move(null);

			assertThatThrownBy(() -> blockService.move(ORIGIN, SCOPE,
					BLOCK.getId(), action))
							.isInstanceOf(MissingEntityException.class);
		}

		@Test
		void originNull() {
			final var action = new BlockAction.Move(null);

			assertThatThrownBy(
					() -> blockService.move(null, SCOPE, BLOCK.getId(), action))
							.isInstanceOf(NullPointerException.class);
		}

		@Test
		void scopeNull() {
			final var action = new BlockAction.Move(null);

			assertThatThrownBy(() -> blockService.move(ORIGIN, null,
					BLOCK.getId(), action))
							.isInstanceOf(NullPointerException.class);
		}

		@Test
		void actionNull() {
			assertThatThrownBy(
					() -> blockService.move(ORIGIN, SCOPE, BLOCK.getId(), null))
							.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class Delete {

		private static final TestBlock BLOCK = TestBlock.SPORER_PROJECT_BLOCKERS_SECOND;
		private static final TestTopic TOPIC = BLOCK.getTopic();
		private static final TestMeeting MEETING = TOPIC.getMeeting();
		private static final long MEETING_ID = MEETING.getId();

		private static final long REVISION = MEETING.getRevision();
		private static final MeetingScope SCOPE = new MeetingScope(MEETING_ID,
				REVISION);

		@BeforeEach
		void block() {
			when(blockStorageGateway.find(BLOCK.getId()))
					.thenReturn(Optional.of(BLOCK.info()));
			when(topicService.getById(PRINCIPAL, MEETING_ID, TOPIC.getId()))
					.thenReturn(TOPIC.info());
		}

		@Test
		void success() {
			blockService.delete(ORIGIN, SCOPE, BLOCK.getId());

			final ArgumentCaptor<BlockEvent> event = ArgumentCaptor
					.forClass(BlockEvent.class);
			verify(eventPublisher).publish(eq(SCOPE), event.capture());

			assertThat(event.getValue().block().getId())
					.isEqualTo(BLOCK.getId());
			assertThat(event.getValue().action())
					.isInstanceOf(BlockAction.Delete.class);

			verify(blockStorageGateway).delete(any());
			verify(blockStorageGateway, never()).update(any());
			verify(blockStorageGateway, never()).findAllByTopicId(anyLong());
		}

		@Test
		void unknownBlock() {
			when(blockStorageGateway.find(anyLong()))
					.thenReturn(Optional.empty());

			assertThatThrownBy(
					() -> blockService.delete(ORIGIN, SCOPE, BLOCK.getId()))
							.isInstanceOf(MissingEntityException.class);
		}

		@Test
		void originNull() {
			assertThatThrownBy(
					() -> blockService.delete(null, SCOPE, BLOCK.getId()))
							.isInstanceOf(NullPointerException.class);
		}

		@Test
		void scopeNull() {
			assertThatThrownBy(
					() -> blockService.delete(ORIGIN, null, BLOCK.getId()))
							.isInstanceOf(NullPointerException.class);
		}
	}
}
