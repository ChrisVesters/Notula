package com.cvesters.notula.topic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.InOrder;

import com.cvesters.notula.common.domain.Origin;
import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.common.exception.MissingEntityException;
import com.cvesters.notula.meeting.EventPublisher;
import com.cvesters.notula.meeting.MeetingService;
import com.cvesters.notula.meeting.TestMeeting;
import com.cvesters.notula.meeting.TestMeetingLock;
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
	private final TestMeetingLock meetingLock = new TestMeetingLock();

	private final TopicStorageGateway topicStorageGateway = mock();
	private final EventPublisher eventPublisher = mock();

	private final TopicService topicService = new TopicService(meetingService,
			meetingLock.lock(), topicStorageGateway, eventPublisher);

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
		private static final Origin ORIGIN = new Origin(SESSION.principal(),
				CLIENT_ID);
		private static final TestMeeting MEETING = TestMeeting.SPORER_PROJECT;
		private static final TestOrganisation ORGANISATION = MEETING
				.getOrganisation();

		private static final long MEETING_ID = MEETING.getId();

		private static final long REVISION = MEETING.getRevision();
		private static final MeetingScope SCOPE = new MeetingScope(MEETING_ID,
				REVISION);

		@BeforeEach
		void revision() {
			meetingLock.passThrough(REVISION);
		}

		private static final long TOPIC_ID = Long.MAX_VALUE;
		private static final int TOPIC_SEQUENCE_ID = 0;
		private static final String TOPIC_NAME = "Topic";
		private static final String TOPIC_DESCRIPTION = "Description";

		@Test
		void firstTopic() {
			when(meetingService.getById(PRINCIPAL, MEETING_ID))
					.thenReturn(MEETING.info());

			when(topicStorageGateway.findAllByMeetingId(MEETING_ID))
					.thenReturn(Collections.emptyList());

			final var created = new TopicInfo(TOPIC_ID, ORGANISATION.getId(),
					MEETING.getId(), TOPIC_SEQUENCE_ID, TOPIC_NAME,
					TOPIC_DESCRIPTION, null);

			when(topicStorageGateway.create(argThat(t -> {
				assertThatThrownBy(t::getId)
						.isInstanceOf(IllegalStateException.class);
				assertThat(t.getOrganisationId())
						.isEqualTo(ORGANISATION.getId());
				assertThat(t.getMeetingId()).isEqualTo(MEETING_ID);
				assertThat(t.getSequenceId()).isEqualTo(TOPIC_SEQUENCE_ID);
				assertThat(t.getName()).isEqualTo(TOPIC_NAME);
				assertThat(t.getDescription()).isEmpty();
				assertThat(t.getDuration()).isEmpty();
				return true;
			}))).thenReturn(created);

			final var action = new TopicAction.Create(TOPIC_SEQUENCE_ID,
					TOPIC_NAME);

			final TopicInfo result = topicService.create(ORIGIN, MEETING_ID,
					action);

			assertThat(result).isEqualTo(created);

			final var expectedAction = new TopicAction.Create(TOPIC_SEQUENCE_ID,
					TOPIC_NAME);
			final var matcher = new TopicActionMatcher.Create(expectedAction);
			final ArgumentMatcher<TopicEvent> event = e -> {
				assertThat(e.origin()).isEqualTo(ORIGIN);
				assertThat(e.topic()).isEqualTo(created);
				assertThat(e.action()).is(matcher.equal());
				return true;
			};

			verify(eventPublisher).publish(eq(SCOPE), argThat(event));
		}

		@Test
		void topicAtEnd() {
			when(meetingService.getById(PRINCIPAL, MEETING_ID))
					.thenReturn(MEETING.info());

			final List<TopicInfo> existingTopics = TestTopic.ofMeeting(MEETING)
					.stream()
					.map(TestTopic::info)
					.toList();
			when(topicStorageGateway.findAllByMeetingId(MEETING_ID))
					.thenReturn(existingTopics);

			final int sequenceId = existingTopics.size();
			final var created = new TopicInfo(TOPIC_ID, ORGANISATION.getId(),
					MEETING.getId(), sequenceId, TOPIC_NAME, TOPIC_DESCRIPTION,
					null);

			when(topicStorageGateway.create(argThat(t -> {
				assertThatThrownBy(t::getId)
						.isInstanceOf(IllegalStateException.class);
				assertThat(t.getOrganisationId())
						.isEqualTo(ORGANISATION.getId());
				assertThat(t.getMeetingId()).isEqualTo(MEETING_ID);
				assertThat(t.getSequenceId()).isEqualTo(sequenceId);
				assertThat(t.getName()).isEqualTo(TOPIC_NAME);
				assertThat(t.getDescription()).isEmpty();
				assertThat(t.getDuration()).isEmpty();
				return true;
			}))).thenReturn(created);

			final var action = new TopicAction.Create(sequenceId, TOPIC_NAME);

			final TopicInfo result = topicService.create(ORIGIN, MEETING_ID,
					action);

			assertThat(result).isEqualTo(created);

			final var expectedAction = new TopicAction.Create(sequenceId,
					TOPIC_NAME);
			final var matcher = new TopicActionMatcher.Create(expectedAction);
			final ArgumentMatcher<TopicEvent> event = e -> {
				assertThat(e.origin()).isEqualTo(ORIGIN);
				assertThat(e.topic()).isEqualTo(created);
				assertThat(e.action()).is(matcher.equal());
				return true;
			};

			verify(eventPublisher).publish(eq(SCOPE), argThat(event));

			verify(topicStorageGateway, never()).update(any());
			verify(topicStorageGateway).create(any());
		}

		@Test
		void topicAtStart() {
			when(meetingService.getById(PRINCIPAL, MEETING_ID))
					.thenReturn(MEETING.info());

			final List<TestTopic> topics = TestTopic.ofMeeting(MEETING);
			final List<TopicInfo> existingTopics = topics.stream()
					.map(TestTopic::info)
					.toList();
			when(topicStorageGateway.findAllByMeetingId(MEETING_ID))
					.thenReturn(existingTopics);
			final List<TopicInfo> updatedTopics = topics.stream()
					.map(t -> mock(TopicInfo.class))
					.toList();

			when(topicStorageGateway.update(any())).thenAnswer(invocation -> {
				final var update = invocation.getArgument(0, TopicInfo.class);

				for (int i = 0; i < topics.size(); i++) {
					final var topic = topics.get(i);
					if (update.getId() != topic.getId()) {
						continue;
					}

					assertThat(update.getSequenceId())
							.isEqualTo(topic.getSequenceId() + 1);
					return updatedTopics.get(i);
				}

				throw new AssertionError("Unexpected update: " + update);
			});

			final int sequenceId = 0;
			final var created = new TopicInfo(TOPIC_ID, ORGANISATION.getId(),
					MEETING.getId(), sequenceId, TOPIC_NAME, TOPIC_DESCRIPTION,
					null);

			when(topicStorageGateway.create(argThat(t -> {
				assertThatThrownBy(t::getId)
						.isInstanceOf(IllegalStateException.class);
				assertThat(t.getOrganisationId())
						.isEqualTo(ORGANISATION.getId());
				assertThat(t.getMeetingId()).isEqualTo(MEETING_ID);
				assertThat(t.getSequenceId()).isZero();
				assertThat(t.getName()).isEqualTo(TOPIC_NAME);
				assertThat(t.getDescription()).isEmpty();
				assertThat(t.getDuration()).isEmpty();
				return true;
			}))).thenReturn(created);

			final var action = new TopicAction.Create(TOPIC_SEQUENCE_ID,
					TOPIC_NAME);

			final TopicInfo result = topicService.create(ORIGIN, MEETING_ID,
					action);

			assertThat(result).isEqualTo(created);

			final ArgumentCaptor<TopicEvent> events = ArgumentCaptor
					.forClass(TopicEvent.class);

			verify(eventPublisher, times(topics.size() + 1)).publish(eq(SCOPE),
					events.capture());

			final List<TopicEvent> topicEvents = events.getAllValues();

			assertThat(topicEvents)
					.allSatisfy(e -> assertThat(e.origin()).isEqualTo(ORIGIN));

			for (int i = 0; i < topics.size(); i++) {
				final var expected = new TopicAction.Move(
						topics.get(i).getSequenceId() + 1);
				final var matcher = new TopicActionMatcher.Move(expected);

				final TopicEvent event = topicEvents.get(i);
				assertThat(event.topic()).isEqualTo(updatedTopics.get(i));
				assertThat(event.action()).is(matcher.equal());
			}

			final var expectedAction = new TopicAction.Create(TOPIC_SEQUENCE_ID,
					TOPIC_NAME);
			final var matcher = new TopicActionMatcher.Create(expectedAction);

			final TopicEvent createEvent = topicEvents.get(topics.size());
			assertThat(createEvent.topic()).isEqualTo(created);
			assertThat(createEvent.action()).is(matcher.equal());

			final InOrder inOrder = inOrder(topicStorageGateway);
			existingTopics.forEach(
					topic -> inOrder.verify(topicStorageGateway).update(topic));
			inOrder.verify(topicStorageGateway).create(any());
		}

		@Test
		void invalidSequenceId() {
			when(meetingService.getById(PRINCIPAL, MEETING_ID))
					.thenReturn(MEETING.info());

			when(topicStorageGateway.findAllByMeetingId(MEETING_ID))
					.thenReturn(Collections.emptyList());

			final var action = new TopicAction.Create(1, TOPIC_NAME);

			assertThatThrownBy(
					() -> topicService.create(ORIGIN, MEETING_ID, action))
							.isInstanceOf(IllegalArgumentException.class);

			verifyNoInteractions(eventPublisher);
			verify(topicStorageGateway, never()).update(any());
			verify(topicStorageGateway, never()).create(any());
		}

		@Test
		void originNull() {
			final TopicAction.Create topic = new TopicAction.Create(
					TOPIC_SEQUENCE_ID, TOPIC_NAME);

			assertThatThrownBy(
					() -> topicService.create(null, MEETING_ID, topic))
							.isInstanceOf(NullPointerException.class);
		}

		@Test
		void actionNull() {
			assertThatThrownBy(
					() -> topicService.create(ORIGIN, MEETING_ID, null))
							.isInstanceOf(NullPointerException.class);
		}

		@Test
		void serialised() {
			meetingLock.withhold();

			final var action = new TopicAction.Create(TOPIC_SEQUENCE_ID,
					TOPIC_NAME);

			topicService.create(ORIGIN, MEETING_ID, action);

			verify(meetingLock.lock()).call(eq(MEETING_ID), any());
			verifyNoInteractions(topicStorageGateway);
		}
	}

	@Nested
	class Move {

		private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
		private static final Origin ORIGIN = new Origin(SESSION.principal(),
				CLIENT_ID);
		private static final TestMeeting MEETING = TestMeeting.SPORER_PROJECT;
		private static final long MEETING_ID = MEETING.getId();

		private static final long REVISION = MEETING.getRevision();
		private static final MeetingScope SCOPE = new MeetingScope(MEETING_ID,
				REVISION);

		@BeforeEach
		void revision() {
			meetingLock.passThrough(REVISION);
		}

		@Test
		void down() {
			final List<TopicInfo> existingTopics = TestTopic.ofMeeting(MEETING)
					.stream()
					.map(TestTopic::info)
					.toList();

			final TopicInfo topic = existingTopics.stream()
					.filter(t -> t.getSequenceId() == 0)
					.findFirst()
					.orElseThrow();
			final TopicInfo updatedTopic = mock();

			final TopicInfo second = existingTopics.stream()
					.filter(t -> t.getSequenceId() == 1)
					.findFirst()
					.orElseThrow();
			final TopicInfo updatedSecond = mock();

			final TopicInfo third = existingTopics.stream()
					.filter(t -> t.getSequenceId() == 2)
					.findFirst()
					.orElseThrow();
			final TopicInfo updatedThird = mock();

			when(topicStorageGateway.find(topic.getId()))
					.thenReturn(Optional.of(topic));

			when(topicStorageGateway.findAllByMeetingId(MEETING.getId()))
					.thenReturn(existingTopics);

			when(topicStorageGateway.update(any())).thenAnswer(invocation -> {
				final var update = invocation.getArgument(0, TopicInfo.class);

				if (update.getId() == topic.getId()
						&& update.getSequenceId() == 2) {
					return updatedTopic;
				}

				if (update.getId() == second.getId()
						&& update.getSequenceId() == 0) {
					return updatedSecond;
				}

				if (update.getId() == third.getId()
						&& update.getSequenceId() == 1) {
					return updatedThird;
				}

				throw new AssertionError("Unexpected update: " + update);
			});

			final var action = new TopicAction.Move(2);
			final TopicInfo result = topicService.move(ORIGIN, MEETING_ID,
					topic.getId(), action);

			assertThat(result).isEqualTo(topic);
			assertThat(result.getSequenceId()).isEqualTo(2);

			final var moved = List.of(result, second, third);

			final ArgumentCaptor<TopicEvent> events = ArgumentCaptor
					.forClass(TopicEvent.class);

			verify(eventPublisher, times(moved.size())).publish(eq(SCOPE),
					events.capture());

			final List<TopicEvent> topicEvents = events.getAllValues();

			assertThat(topicEvents).hasSameSizeAs(moved)
					.allSatisfy(event -> assertThat(event.origin())
							.isEqualTo(ORIGIN))
					.satisfiesExactlyInAnyOrder(event -> {
						assertThat(event.topic()).isEqualTo(updatedTopic);
						final var expected = new TopicAction.Move(2);
						final var matcher = new TopicActionMatcher.Move(
								expected);
						assertThat(event.action()).is(matcher.equal());
					}, event -> {
						assertThat(event.topic()).isEqualTo(updatedSecond);
						final var expected = new TopicAction.Move(0);
						final var matcher = new TopicActionMatcher.Move(
								expected);
						assertThat(event.action()).is(matcher.equal());
					}, event -> {
						assertThat(event.topic()).isEqualTo(updatedThird);
						final var expected = new TopicAction.Move(1);
						final var matcher = new TopicActionMatcher.Move(
								expected);
						assertThat(event.action()).is(matcher.equal());
					});
		}

		@Test
		void up() {
			final List<TopicInfo> existingTopics = TestTopic.ofMeeting(MEETING)
					.stream()
					.map(TestTopic::info)
					.toList();

			final TopicInfo topic = existingTopics.stream()
					.filter(t -> t.getSequenceId() == 2)
					.findFirst()
					.orElseThrow();
			final TopicInfo updatedTopic = mock();

			final TopicInfo second = existingTopics.stream()
					.filter(t -> t.getSequenceId() == 1)
					.findFirst()
					.orElseThrow();
			final TopicInfo updatedSecond = mock();

			final TopicInfo first = existingTopics.stream()
					.filter(t -> t.getSequenceId() == 0)
					.findFirst()
					.orElseThrow();
			final TopicInfo updatedFirst = mock();

			when(topicStorageGateway.find(topic.getId()))
					.thenReturn(Optional.of(topic));

			when(topicStorageGateway.findAllByMeetingId(MEETING.getId()))
					.thenReturn(existingTopics);

			when(topicStorageGateway.update(any())).thenAnswer(invocation -> {
				final var update = invocation.getArgument(0, TopicInfo.class);

				if (update.getId() == topic.getId()
						&& update.getSequenceId() == 0) {
					return updatedTopic;
				}

				if (update.getId() == first.getId()
						&& update.getSequenceId() == 1) {
					return updatedFirst;
				}

				if (update.getId() == second.getId()
						&& update.getSequenceId() == 2) {
					return updatedSecond;
				}

				throw new AssertionError("Unexpected update: " + update);
			});

			final var action = new TopicAction.Move(0);
			final TopicInfo result = topicService.move(ORIGIN, MEETING_ID,
					topic.getId(), action);

			assertThat(result).isEqualTo(topic);
			assertThat(result.getSequenceId()).isZero();

			final var moved = List.of(result, second, first);
			final ArgumentCaptor<TopicEvent> events = ArgumentCaptor
					.forClass(TopicEvent.class);
			verify(eventPublisher, times(moved.size())).publish(eq(SCOPE),
					events.capture());

			final List<TopicEvent> topicEvents = events.getAllValues();

			assertThat(topicEvents).hasSameSizeAs(moved)
					.allSatisfy(event -> assertThat(event.origin())
							.isEqualTo(ORIGIN))
					.satisfiesExactlyInAnyOrder(event -> {
						assertThat(event.topic()).isEqualTo(updatedTopic);
						final var expected = new TopicAction.Move(0);
						final var matcher = new TopicActionMatcher.Move(
								expected);
						assertThat(event.action()).is(matcher.equal());
					}, event -> {
						assertThat(event.topic()).isEqualTo(updatedFirst);
						final var expected = new TopicAction.Move(1);
						final var matcher = new TopicActionMatcher.Move(
								expected);
						assertThat(event.action()).is(matcher.equal());
					}, event -> {
						assertThat(event.topic()).isEqualTo(updatedSecond);
						final var expected = new TopicAction.Move(2);
						final var matcher = new TopicActionMatcher.Move(
								expected);
						assertThat(event.action()).is(matcher.equal());
					});
		}

		@Test
		void neighbour() {
			final List<TopicInfo> existingTopics = TestTopic.ofMeeting(MEETING)
					.stream()
					.map(TestTopic::info)
					.toList();

			final TopicInfo topic = existingTopics.stream()
					.filter(t -> t.getSequenceId() == 1)
					.findFirst()
					.orElseThrow();
			final TopicInfo updatedTopic = mock();

			final TopicInfo neighbour = existingTopics.stream()
					.filter(t -> t.getSequenceId() == 2)
					.findFirst()
					.orElseThrow();
			final TopicInfo updatedNeighbour = mock();

			when(topicStorageGateway.find(topic.getId()))
					.thenReturn(Optional.of(topic));

			when(topicStorageGateway.findAllByMeetingId(MEETING.getId()))
					.thenReturn(existingTopics);

			when(topicStorageGateway.update(any())).thenAnswer(invocation -> {
				final var update = invocation.getArgument(0, TopicInfo.class);

				if (update.getId() == topic.getId()
						&& update.getSequenceId() == 2) {
					return updatedTopic;
				}

				if (update.getId() == neighbour.getId()
						&& update.getSequenceId() == 1) {
					return updatedNeighbour;
				}

				throw new AssertionError("Unexpected update: " + update);
			});

			final var action = new TopicAction.Move(2);
			final TopicInfo result = topicService.move(ORIGIN, MEETING_ID,
					topic.getId(), action);

			assertThat(result).isEqualTo(topic);
			assertThat(result.getSequenceId()).isEqualTo(2);

			final var moved = List.of(neighbour, result);
			final ArgumentCaptor<TopicEvent> events = ArgumentCaptor
					.forClass(TopicEvent.class);
			verify(eventPublisher, times(moved.size())).publish(eq(SCOPE),
					events.capture());

			final List<TopicEvent> topicEvents = events.getAllValues();

			assertThat(topicEvents).hasSameSizeAs(moved)
					.allSatisfy(event -> assertThat(event.origin())
							.isEqualTo(ORIGIN))
					.satisfiesExactlyInAnyOrder(event -> {
						assertThat(event.topic()).isEqualTo(updatedTopic);
						final var expected = new TopicAction.Move(2);
						final var matcher = new TopicActionMatcher.Move(
								expected);
						assertThat(event.action()).is(matcher.equal());
					}, event -> {
						assertThat(event.topic()).isEqualTo(updatedNeighbour);
						final var expected = new TopicAction.Move(1);
						final var matcher = new TopicActionMatcher.Move(
								expected);
						assertThat(event.action()).is(matcher.equal());
					});
		}

		@Test
		void unchanged() {
			final TopicInfo topic = TestTopic.SPORER_PROJECT_BLOCKERS.info();
			final long topicId = topic.getId();
			when(topicStorageGateway.find(topicId))
					.thenReturn(Optional.of(topic));

			final var action = new TopicAction.Move(topic.getSequenceId());
			final TopicInfo result = topicService.move(ORIGIN, MEETING_ID,
					topicId, action);

			assertThat(result).isEqualTo(topic);
			verifyNoInteractions(eventPublisher);
			verify(topicStorageGateway, never()).update(any());
			verify(topicStorageGateway, never()).findAllByMeetingId(anyLong());
		}

		@Test
		void sequenceIdTooLarge() {
			final List<TopicInfo> existingTopics = TestTopic.ofMeeting(MEETING)
					.stream()
					.map(TestTopic::info)
					.toList();

			final TopicInfo topic = existingTopics.getFirst();
			final long topicId = topic.getId();
			when(topicStorageGateway.find(topicId))
					.thenReturn(Optional.of(topic));

			when(topicStorageGateway.findAllByMeetingId(MEETING.getId()))
					.thenReturn(existingTopics);

			final var action = new TopicAction.Move(existingTopics.size());

			assertThatThrownBy(() -> topicService.move(ORIGIN, MEETING_ID,
					topicId, action))
							.isInstanceOf(IllegalArgumentException.class);

			verifyNoInteractions(eventPublisher);
			verify(topicStorageGateway, never()).update(any());
		}

		@Test
		void notFound() {
			final long topicId = TestTopic.SPORER_PROJECT_BLOCKERS.getId();
			when(topicStorageGateway.find(topicId))
					.thenReturn(Optional.empty());

			final var action = new TopicAction.Move(1);

			assertThatThrownBy(() -> topicService.move(ORIGIN, MEETING_ID,
					topicId, action))
							.isInstanceOf(MissingEntityException.class);

			verifyNoInteractions(eventPublisher);
			verify(topicStorageGateway, never()).update(any());
		}

		@Test
		void otherOrganisation() {
			final var origin = new Origin(
					TestSession.ALISON_DACH_GLOVER.principal());

			final TopicInfo topic = TestTopic.SPORER_PROJECT_BLOCKERS.info();
			final long topicId = topic.getId();
			when(topicStorageGateway.find(topicId))
					.thenReturn(Optional.of(topic));

			final var action = new TopicAction.Move(2);

			assertThatThrownBy(() -> topicService.move(origin, MEETING_ID,
					topicId, action))
							.isInstanceOf(MissingEntityException.class);

			verifyNoInteractions(eventPublisher);
			verify(topicStorageGateway, never()).update(any());
		}

		@Test
		void originNull() {
			final long topicId = TestTopic.SPORER_PROJECT_BLOCKERS.getId();
			final var action = new TopicAction.Move(1);

			assertThatThrownBy(
					() -> topicService.move(null, MEETING_ID, topicId, action))
							.isInstanceOf(NullPointerException.class);
		}

		@Test
		void actionNull() {
			final long topicId = TestTopic.SPORER_PROJECT_BLOCKERS.getId();

			assertThatThrownBy(
					() -> topicService.move(ORIGIN, MEETING_ID, topicId, null))
							.isInstanceOf(NullPointerException.class);
		}

		@Test
		void serialised() {
			final TestTopic topic = TestTopic.SPORER_PROJECT_BLOCKERS;

			meetingLock.withhold();

			topicService.move(ORIGIN, MEETING_ID, topic.getId(),
					new TopicAction.Move(1));

			verify(meetingLock.lock()).call(eq(MEETING_ID), any());

			verifyNoInteractions(topicStorageGateway);
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

		@BeforeEach
		void revision() {
			meetingLock.passThrough(REVISION);
		}

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
				assertThat(info.getSequenceId())
						.isEqualTo(TOPIC.getSequenceId());
				assertThat(info.getName()).isEqualTo("Project Timeline");
				assertThat(info.getDescription())
						.isEqualTo(TOPIC.getDescription());
				return true;
			}))).thenReturn(updated);

			final TopicAction.Update action = new TopicAction.UpdateName(0, 0,
					"Project ");
			final TopicInfo result = topicService.update(ORIGIN, MEETING_ID,
					topicId, action);

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

			assertThatThrownBy(() -> topicService.update(ORIGIN, MEETING_ID,
					topicId, action))
							.isInstanceOf(MissingEntityException.class);

			verify(topicStorageGateway, never()).update(any());
			verifyNoInteractions(eventPublisher);
		}

		@Test
		void originNull() {
			final long topicId = TOPIC.getId();

			final TopicAction.Update action = new TopicAction.UpdateName(0, 0,
					"Project ");

			assertThatThrownBy(() -> topicService.update(null, MEETING_ID,
					topicId, action)).isInstanceOf(NullPointerException.class);
		}

		@Test
		void actionNull() {
			final long topicId = TOPIC.getId();

			assertThatThrownBy(() -> topicService.update(ORIGIN, MEETING_ID,
					topicId, null)).isInstanceOf(NullPointerException.class);
		}

		@Test
		void serialised() {
			meetingLock.withhold();

			final var action = new TopicAction.UpdateName(0, 0, "Renamed");

			topicService.update(ORIGIN, MEETING_ID, TOPIC.getId(), action);

			verify(meetingLock.lock()).call(eq(MEETING_ID), any());

			verifyNoInteractions(topicStorageGateway);
		}
	}

	@Nested
	class Delete {

		private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
		private static final Origin ORIGIN = new Origin(SESSION.principal(),
				CLIENT_ID);
		private static final TestTopic TOPIC = TestTopic.SPORER_PROJECT_TIMELINE;
		private static final TestMeeting MEETING = TOPIC.getMeeting();
		private static final long MEETING_ID = MEETING.getId();

		private static final long REVISION = MEETING.getRevision();
		private static final MeetingScope SCOPE = new MeetingScope(MEETING_ID,
				REVISION);

		@BeforeEach
		void revision() {
			meetingLock.passThrough(REVISION);
		}

		@Test
		void onlyTopic() {
			final long meetingId = MEETING.getId();
			final long topicId = TOPIC.getId();

			final TopicInfo topicInfo = TOPIC.info();
			when(topicStorageGateway.find(topicId))
					.thenReturn(Optional.of(topicInfo));

			when(topicStorageGateway.findAllByMeetingId(meetingId))
					.thenReturn(List.of(topicInfo));

			topicService.delete(ORIGIN, MEETING_ID, topicId);

			verify(topicStorageGateway).delete(topicInfo);
			final ArgumentMatcher<TopicEvent> event = e -> {
				assertThat(e.origin()).isEqualTo(ORIGIN);
				assertThat(e.topic()).isEqualTo(topicInfo);
				assertThat(e.action()).isInstanceOf(TopicAction.Delete.class);
				return true;
			};

			verify(eventPublisher).publish(eq(SCOPE), argThat(event));

			verify(topicStorageGateway, never()).update(any());
		}

		@Test
		void firstTopic() {
			final TestMeeting meeting = TestMeeting.SPORER_PROJECT;
			final List<TestTopic> topics = TestTopic.ofMeeting(MEETING);
			final TestTopic deleted = topics.getFirst();

			final long meetingId = meeting.getId();
			final long topicId = deleted.getId();

			final TopicInfo topicInfo = deleted.info();
			when(topicStorageGateway.find(topicId))
					.thenReturn(Optional.of(topicInfo));

			final List<TopicInfo> existingTopics = topics.stream()
					.map(TestTopic::info)
					.toList();
			when(topicStorageGateway.findAllByMeetingId(meetingId))
					.thenReturn(existingTopics);

			final List<TestTopic> movedTopics = topics.stream()
					.filter(t -> t.getSequenceId() > deleted.getSequenceId())
					.toList();
			final List<TopicInfo> updatedTopics = movedTopics.stream()
					.map(t -> mock(TopicInfo.class))
					.toList();

			when(topicStorageGateway.update(any())).thenAnswer(invocation -> {
				final var update = invocation.getArgument(0, TopicInfo.class);

				for (int i = 0; i < movedTopics.size(); i++) {
					final var movedTopic = movedTopics.get(i);
					if (update.getId() != movedTopic.getId()) {
						continue;
					}

					assertThat(update.getSequenceId())
							.isEqualTo(movedTopic.getSequenceId() - 1);
					return updatedTopics.get(i);
				}

				throw new AssertionError("Unexpected update: " + update);
			});

			topicService.delete(ORIGIN, MEETING_ID, topicId);

			final ArgumentCaptor<TopicEvent> events = ArgumentCaptor
					.forClass(TopicEvent.class);

			verify(eventPublisher, times(movedTopics.size() + 1))
					.publish(eq(SCOPE), events.capture());

			final List<TopicEvent> topicEvents = events.getAllValues();

			assertThat(topicEvents)
					.allSatisfy(e -> assertThat(e.origin()).isEqualTo(ORIGIN));

			final TopicEvent deleteEvent = topicEvents.getFirst();
			assertThat(deleteEvent.topic()).isEqualTo(topicInfo);
			assertThat(deleteEvent.action())
					.isInstanceOf(TopicAction.Delete.class);

			for (int i = 0; i < movedTopics.size(); i++) {
				final var expected = new TopicAction.Move(
						movedTopics.get(i).getSequenceId() - 1);
				final var matcher = new TopicActionMatcher.Move(expected);

				final TopicEvent event = topicEvents.get(i + 1);
				assertThat(event.topic()).isEqualTo(updatedTopics.get(i));
				assertThat(event.action()).is(matcher.equal());
			}

			final InOrder inOrder = inOrder(topicStorageGateway);
			inOrder.verify(topicStorageGateway).delete(topicInfo);
			movedTopics.forEach(t -> inOrder.verify(topicStorageGateway)
					.update(argThat(u -> u.getId() == t.getId())));
		}

		@Test
		void lastTopic() {
			final TestMeeting meeting = TestMeeting.SPORER_PROJECT;
			final List<TestTopic> topics = TestTopic.ofMeeting(MEETING);
			final TestTopic deleted = topics.getLast();

			final long meetingId = meeting.getId();
			final long topicId = deleted.getId();

			final TopicInfo topicInfo = deleted.info();
			when(topicStorageGateway.find(topicId))
					.thenReturn(Optional.of(topicInfo));

			final List<TopicInfo> existingTopics = topics.stream()
					.map(TestTopic::info)
					.toList();
			when(topicStorageGateway.findAllByMeetingId(meetingId))
					.thenReturn(existingTopics);

			topicService.delete(ORIGIN, MEETING_ID, topicId);

			verify(topicStorageGateway).delete(topicInfo);
			final ArgumentMatcher<TopicEvent> event = e -> {
				assertThat(e.origin()).isEqualTo(ORIGIN);
				assertThat(e.topic()).isEqualTo(topicInfo);
				assertThat(e.action()).isInstanceOf(TopicAction.Delete.class);
				return true;
			};

			verify(eventPublisher).publish(eq(SCOPE), argThat(event));

			verify(topicStorageGateway, never()).update(any());
		}

		@Test
		void notFound() {
			final long topicId = TOPIC.getId();

			when(topicStorageGateway.find(topicId))
					.thenReturn(Optional.empty());

			assertThatThrownBy(
					() -> topicService.delete(ORIGIN, MEETING_ID, topicId))
							.isInstanceOf(MissingEntityException.class);

			verify(topicStorageGateway, never()).delete(any());
			verifyNoInteractions(eventPublisher);
		}

		@Test
		void originNull() {
			final long topicId = TOPIC.getId();

			assertThatThrownBy(
					() -> topicService.delete(null, MEETING_ID, topicId))
							.isInstanceOf(NullPointerException.class);
		}

		@Test
		void serialised() {
			meetingLock.withhold();

			topicService.delete(ORIGIN, MEETING_ID, TOPIC.getId());

			verify(meetingLock.lock()).run(eq(MEETING_ID), any());

			verifyNoInteractions(topicStorageGateway);
		}
	}
}
