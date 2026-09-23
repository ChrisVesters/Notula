package com.cvesters.notula.topic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
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
import org.mockito.ArgumentMatcher;

import com.cvesters.notula.common.domain.Origin;
import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.common.exception.MissingEntityException;
import com.cvesters.notula.meeting.EventPublisher;
import com.cvesters.notula.meeting.MeetingService;
import com.cvesters.notula.meeting.TestMeeting;
import com.cvesters.notula.meeting.bdo.MeetingScope;
import com.cvesters.notula.organisation.TestOrganisation;
import com.cvesters.notula.session.TestSession;
import com.cvesters.notula.topic.bdo.TopicAction;
import com.cvesters.notula.topic.bdo.TopicEvent;
import com.cvesters.notula.topic.bdo.TopicInfo;

class TopicServiceTest {

	private static final UUID CLIENT_ID = UUID
			.fromString("3f9c1a44-1d2e-4a51-8b0c-2c7e9b1d4a0e");

	private final MeetingService meetingService = mock();

	private final TopicStorageGateway topicStorageGateway = mock();
	private final EventPublisher eventPublisher = mock();

	private final TopicService topicService = new TopicService(meetingService,
			topicStorageGateway, eventPublisher);

	@Nested
	class GetById {

		private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
		private static final Principal PRINCIPAL = SESSION.principal();
		private static final TestTopic TOPIC = TestTopic.SPORER_PROJECT_TIMELINE;

		@Test
		void success() {
			final TopicInfo topicInfo = TOPIC.info();
			when(topicStorageGateway.find(TOPIC.getId()))
					.thenReturn(Optional.of(topicInfo));

			final TopicInfo result = topicService.getById(PRINCIPAL,
					TOPIC.getId());

			assertThat(result).isEqualTo(topicInfo);
		}

		@Test
		void notFound() {
			final long topicId = TOPIC.getId();

			when(topicStorageGateway.find(topicId))
					.thenReturn(Optional.empty());

			assertThatThrownBy(() -> topicService.getById(PRINCIPAL, topicId))
					.isInstanceOf(MissingEntityException.class);
		}

		@Test
		void otherOrganisation() {
			final Principal principal = TestSession.ALISON_DACH_GLOVER
					.principal();
			final long topicId = TOPIC.getId();

			final TopicInfo topicInfo = TOPIC.info();
			when(topicStorageGateway.find(topicId))
					.thenReturn(Optional.of(topicInfo));

			assertThatThrownBy(() -> topicService.getById(principal, topicId))
					.isInstanceOf(MissingEntityException.class);
		}

		@Test
		void principalNull() {
			final long topicId = TOPIC.getId();

			assertThatThrownBy(() -> topicService.getById(null, topicId))
					.isInstanceOf(NullPointerException.class);
		}

		@Test
		void inMeeting() {
			final long meetingId = TOPIC.getMeeting().getId();

			final TopicInfo topicInfo = TOPIC.info();
			when(topicStorageGateway.find(TOPIC.getId()))
					.thenReturn(Optional.of(topicInfo));

			final TopicInfo result = topicService.getById(PRINCIPAL, meetingId,
					TOPIC.getId());

			assertThat(result).isEqualTo(topicInfo);
		}

		@Test
		void otherMeeting() {
			final long meetingId = TestMeeting.SPORER_RETRO.getId();
			final long topicId = TOPIC.getId();

			when(topicStorageGateway.find(topicId))
					.thenReturn(Optional.of(TOPIC.info()));

			assertThatThrownBy(
					() -> topicService.getById(PRINCIPAL, meetingId, topicId))
							.isInstanceOf(MissingEntityException.class);
		}
	}

	@Nested
	class Create {

		private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
		private static final Principal PRINCIPAL = SESSION.principal();
		private static final Origin ORIGIN = new Origin(PRINCIPAL, CLIENT_ID);
		private static final TestMeeting MEETING = TestMeeting.SPORER_PROJECT;
		private static final TestOrganisation ORGANISATION = MEETING
				.getOrganisation();

		private static final long MEETING_ID = MEETING.getId();

		private static final long REVISION = MEETING.getRevision();
		private static final MeetingScope SCOPE = new MeetingScope(MEETING_ID,
				REVISION);

		private static final String NAME = "Blockers";

		private final List<TopicInfo> siblings = TestTopic.ofMeeting(MEETING)
				.stream()
				.map(TestTopic::info)
				.toList();

		@BeforeEach
		void setup() {
			when(meetingService.getById(PRINCIPAL, MEETING_ID))
					.thenReturn(MEETING.info());
			when(topicStorageGateway.findAllByMeetingId(MEETING_ID))
					.thenReturn(siblings);
			when(topicStorageGateway.create(any()))
					.thenAnswer(invocation -> invocation.getArgument(0));
		}

		@Test
		void between() {
			final TopicInfo after = siblings.get(0);
			final TopicInfo next = siblings.get(1);

			final var action = new TopicAction.Create(after.getId(), NAME);

			final TopicInfo created = topicService.create(ORIGIN, SCOPE,
					action);

			assertThat(created.getRank()).isGreaterThan(after.getRank())
					.isLessThan(next.getRank());
			assertThat(created.getOrganisationId())
					.isEqualTo(ORGANISATION.getId());
			assertThat(created.getMeetingId()).isEqualTo(MEETING_ID);
			assertThat(created.getName()).isEqualTo(NAME);

			verify(topicStorageGateway).create(created);
			verify(topicStorageGateway, never()).update(any());
			verify(eventPublisher).publish(SCOPE,
					new TopicEvent(created, action, ORIGIN));
			verifyNoMoreInteractions(eventPublisher);
		}

		@Test
		void first() {
			final var action = new TopicAction.Create(null, NAME);

			final TopicInfo created = topicService.create(ORIGIN, SCOPE,
					action);

			assertThat(created.getRank())
					.isLessThan(siblings.getFirst().getRank());

			verify(topicStorageGateway).create(created);
			verify(topicStorageGateway, never()).update(any());
			verify(eventPublisher).publish(SCOPE,
					new TopicEvent(created, action, ORIGIN));
			verifyNoMoreInteractions(eventPublisher);
		}

		@Test
		void last() {
			final TopicInfo last = siblings.getLast();

			final var action = new TopicAction.Create(last.getId(), NAME);

			final TopicInfo created = topicService.create(ORIGIN, SCOPE,
					action);

			assertThat(created.getRank()).isGreaterThan(last.getRank());

			verify(topicStorageGateway).create(created);
			verify(topicStorageGateway, never()).update(any());
			verify(eventPublisher).publish(SCOPE,
					new TopicEvent(created, action, ORIGIN));
			verifyNoMoreInteractions(eventPublisher);
		}

		@Test
		void only() {
			when(topicStorageGateway.findAllByMeetingId(MEETING_ID))
					.thenReturn(List.of());

			final var action = new TopicAction.Create(null, NAME);

			final TopicInfo created = topicService.create(ORIGIN, SCOPE,
					action);

			assertThat(created.getRank()).isNotNull();

			verify(topicStorageGateway).create(created);
			verify(topicStorageGateway, never()).update(any());
			verify(eventPublisher).publish(SCOPE,
					new TopicEvent(created, action, ORIGIN));
			verifyNoMoreInteractions(eventPublisher);
		}

		@Test
		void unknownNeighbour() {
			final var action = new TopicAction.Create(Long.MAX_VALUE, NAME);

			assertThatThrownBy(() -> topicService.create(ORIGIN, SCOPE, action))
					.isInstanceOf(MissingEntityException.class);

			verify(topicStorageGateway, never()).create(any());
			verifyNoInteractions(eventPublisher);
		}

		@Test
		void originNull() {
			final var action = new TopicAction.Create(null, NAME);

			assertThatThrownBy(() -> topicService.create(null, SCOPE, action))
					.isInstanceOf(NullPointerException.class);
		}

		@Test
		void scopeNull() {
			final var action = new TopicAction.Create(null, NAME);

			assertThatThrownBy(() -> topicService.create(ORIGIN, null, action))
					.isInstanceOf(NullPointerException.class);
		}

		@Test
		void actionNull() {
			assertThatThrownBy(() -> topicService.create(ORIGIN, SCOPE, null))
					.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class Move {

		private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
		private static final Principal PRINCIPAL = SESSION.principal();
		private static final Origin ORIGIN = new Origin(PRINCIPAL, CLIENT_ID);
		private static final TestMeeting MEETING = TestMeeting.SPORER_PROJECT;
		private static final TestTopic TOPIC = TestTopic.SPORER_PROJECT_TIMELINE;

		private static final long MEETING_ID = MEETING.getId();

		private static final long REVISION = MEETING.getRevision();
		private static final MeetingScope SCOPE = new MeetingScope(MEETING_ID,
				REVISION);

		private final List<TopicInfo> siblings = TestTopic.ofMeeting(MEETING)
				.stream()
				.map(TestTopic::info)
				.toList();

		@BeforeEach
		void topics() {
			when(topicStorageGateway.find(TOPIC.getId()))
					.thenReturn(Optional.of(TOPIC.info()));
			when(topicStorageGateway.findAllByMeetingId(MEETING_ID))
					.thenReturn(siblings);
			when(topicStorageGateway.update(any()))
					.thenAnswer(invocation -> invocation.getArgument(0));
		}

		@Test
		void between() {
			final TopicInfo after = siblings.get(0);
			final TopicInfo next = siblings.get(1);

			final var action = new TopicAction.Move(after.getId());

			final TopicInfo moved = topicService.move(ORIGIN, SCOPE,
					TOPIC.getId(), action);

			assertThat(moved.getId()).isEqualTo(TOPIC.getId());
			assertThat(moved.getRank()).isGreaterThan(after.getRank())
					.isLessThan(next.getRank());

			verify(topicStorageGateway).update(moved);
			verify(topicStorageGateway, never()).create(any());
			verify(eventPublisher).publish(SCOPE,
					new TopicEvent(moved, action, ORIGIN));
			verifyNoMoreInteractions(eventPublisher);
		}

		@Test
		void first() {
			final var action = new TopicAction.Move(null);

			final TopicInfo moved = topicService.move(ORIGIN, SCOPE,
					TOPIC.getId(), action);

			assertThat(moved.getRank())
					.isLessThan(siblings.getFirst().getRank());

			verify(topicStorageGateway).update(moved);
			verify(topicStorageGateway, never()).create(any());
			verify(eventPublisher).publish(SCOPE,
					new TopicEvent(moved, action, ORIGIN));
			verifyNoMoreInteractions(eventPublisher);
		}

		// Its own rank is excluded, so following the topic above it is a move
		// to where it already is rather than a refusal or a rank collision.
		@Test
		void inPlace() {
			final TopicInfo above = siblings.get(siblings.size() - 2);

			final var action = new TopicAction.Move(above.getId());

			final TopicInfo moved = topicService.move(ORIGIN, SCOPE,
					TOPIC.getId(), action);

			assertThat(moved.getRank()).isGreaterThan(above.getRank());

			verify(topicStorageGateway).update(moved);
			verify(topicStorageGateway, never()).create(any());
			verify(eventPublisher).publish(SCOPE,
					new TopicEvent(moved, action, ORIGIN));
			verifyNoMoreInteractions(eventPublisher);
		}

		@Test
		void unknownNeighbour() {
			final var action = new TopicAction.Move(Long.MAX_VALUE);

			assertThatThrownBy(() -> topicService.move(ORIGIN, SCOPE,
					TOPIC.getId(), action))
							.isInstanceOf(MissingEntityException.class);

			verify(topicStorageGateway, never()).update(any());
			verifyNoInteractions(eventPublisher);
		}

		@Test
		void unknownTopic() {
			when(topicStorageGateway.find(anyLong()))
					.thenReturn(Optional.empty());

			final var action = new TopicAction.Move(null);

			assertThatThrownBy(() -> topicService.move(ORIGIN, SCOPE,
					TOPIC.getId(), action))
							.isInstanceOf(MissingEntityException.class);
		}

		@Test
		void originNull() {
			final var action = new TopicAction.Move(null);

			assertThatThrownBy(
					() -> topicService.move(null, SCOPE, TOPIC.getId(), action))
							.isInstanceOf(NullPointerException.class);
		}

		@Test
		void scopeNull() {
			final var action = new TopicAction.Move(null);

			assertThatThrownBy(() -> topicService.move(ORIGIN, null,
					TOPIC.getId(), action))
							.isInstanceOf(NullPointerException.class);
		}

		@Test
		void actionNull() {
			assertThatThrownBy(
					() -> topicService.move(ORIGIN, SCOPE, TOPIC.getId(), null))
							.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class Update {

		private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
		private static final Origin ORIGIN = new Origin(SESSION.principal(),
				CLIENT_ID);
		private static final TestTopic TOPIC = TestTopic.SPORER_PROJECT_TIMELINE;
		private static final TestMeeting MEETING = TOPIC.getMeeting();
		private static final long MEETING_ID = MEETING.getId();

		private static final long REVISION = MEETING.getRevision();
		private static final MeetingScope SCOPE = new MeetingScope(MEETING_ID,
				REVISION);

		@Test
		void success() {
			final long meetingId = MEETING.getId();
			final long topicId = TOPIC.getId();

			final TopicInfo topicInfo = TOPIC.info();
			when(topicStorageGateway.find(topicId))
					.thenReturn(Optional.of(topicInfo));

			final TopicInfo updated = mock();
			when(topicStorageGateway.update(argThat(info -> {
				assertThat(info.getId()).isEqualTo(topicId);
				assertThat(info.getOrganisationId())
						.isEqualTo(MEETING.getOrganisation().getId());
				assertThat(info.getMeetingId()).isEqualTo(meetingId);
				assertThat(info.getRank()).isEqualTo(TOPIC.getRank());
				assertThat(info.getName()).isEqualTo("Project Timeline");
				assertThat(info.getDescription())
						.isEqualTo(TOPIC.getDescription());
				return true;
			}))).thenReturn(updated);

			final TopicAction.Update action = new TopicAction.UpdateName(0, 0,
					"Project ");
			final TopicInfo result = topicService.update(ORIGIN, SCOPE, topicId,
					action);

			assertThat(result).isEqualTo(updated);

			final ArgumentMatcher<TopicEvent> event = e -> {
				assertThat(e.origin()).isEqualTo(ORIGIN);
				assertThat(e.topic()).isEqualTo(updated);
				assertThat(e.action()).isEqualTo(action);
				return true;
			};

			verify(eventPublisher).publish(eq(SCOPE), argThat(event));
		}

		@Test
		void notFound() {
			final long topicId = TOPIC.getId();

			when(topicStorageGateway.find(topicId))
					.thenReturn(Optional.empty());

			final TopicAction.Update action = new TopicAction.UpdateName(0, 0,
					"Project ");

			assertThatThrownBy(
					() -> topicService.update(ORIGIN, SCOPE, topicId, action))
							.isInstanceOf(MissingEntityException.class);

			verify(topicStorageGateway, never()).update(any());
			verifyNoInteractions(eventPublisher);
		}

		@Test
		void originNull() {
			final long topicId = TOPIC.getId();

			final TopicAction.Update action = new TopicAction.UpdateName(0, 0,
					"Project ");

			assertThatThrownBy(
					() -> topicService.update(null, SCOPE, topicId, action))
							.isInstanceOf(NullPointerException.class);
		}

		@Test
		void scopeNull() {
			final long topicId = TOPIC.getId();

			final TopicAction.Update action = new TopicAction.UpdateName(0, 0,
					"Project ");

			assertThatThrownBy(
					() -> topicService.update(ORIGIN, null, topicId, action))
							.isInstanceOf(NullPointerException.class);
		}

		@Test
		void actionNull() {
			final long topicId = TOPIC.getId();

			assertThatThrownBy(
					() -> topicService.update(ORIGIN, SCOPE, topicId, null))
							.isInstanceOf(NullPointerException.class);
		}

	}

	@Nested
	class Delete {

		private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
		private static final Principal PRINCIPAL = SESSION.principal();
		private static final Origin ORIGIN = new Origin(PRINCIPAL, CLIENT_ID);
		private static final TestMeeting MEETING = TestMeeting.SPORER_PROJECT;
		private static final TestTopic TOPIC = TestTopic.SPORER_PROJECT_BLOCKERS;

		private static final long MEETING_ID = MEETING.getId();

		private static final long REVISION = MEETING.getRevision();
		private static final MeetingScope SCOPE = new MeetingScope(MEETING_ID,
				REVISION);

		@BeforeEach
		void topic() {
			when(topicStorageGateway.find(TOPIC.getId()))
					.thenReturn(Optional.of(TOPIC.info()));
		}

		// Nothing below it has to move up any more, which is what ranks buy.
		@Test
		void success() {
			topicService.delete(ORIGIN, SCOPE, TOPIC.getId());

			final ArgumentCaptor<TopicEvent> event = ArgumentCaptor
					.forClass(TopicEvent.class);
			verify(eventPublisher).publish(eq(SCOPE), event.capture());

			assertThat(event.getValue().topic().getId())
					.isEqualTo(TOPIC.getId());
			assertThat(event.getValue().action())
					.isInstanceOf(TopicAction.Delete.class);

			verify(topicStorageGateway).delete(any());
			verify(topicStorageGateway, never()).update(any());
			verify(topicStorageGateway, never()).findAllByMeetingId(anyLong());
		}

		@Test
		void unknownTopic() {
			when(topicStorageGateway.find(anyLong()))
					.thenReturn(Optional.empty());

			assertThatThrownBy(
					() -> topicService.delete(ORIGIN, SCOPE, TOPIC.getId()))
							.isInstanceOf(MissingEntityException.class);
		}

		@Test
		void originNull() {
			assertThatThrownBy(
					() -> topicService.delete(null, SCOPE, TOPIC.getId()))
							.isInstanceOf(NullPointerException.class);
		}

		@Test
		void scopeNull() {
			assertThatThrownBy(
					() -> topicService.delete(ORIGIN, null, TOPIC.getId()))
							.isInstanceOf(NullPointerException.class);
		}
	}
}
