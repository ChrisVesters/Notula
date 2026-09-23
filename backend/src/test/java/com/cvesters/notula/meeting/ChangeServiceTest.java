package com.cvesters.notula.meeting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.block.BlockService;
import com.cvesters.notula.block.bdo.BlockType;
import com.cvesters.notula.common.domain.Origin;
import com.cvesters.notula.common.domain.TextUpdate;
import com.cvesters.notula.meeting.bdo.MeetingScope;
import com.cvesters.notula.meeting.dto.BlockChangeDto;
import com.cvesters.notula.meeting.dto.MeetingChangeDto;
import com.cvesters.notula.meeting.dto.TextBlockChangeDto;
import com.cvesters.notula.meeting.dto.TextEditDto;
import com.cvesters.notula.meeting.dto.TopicChangeDto;
import com.cvesters.notula.session.TestSession;
import com.cvesters.notula.textblock.TextBlockService;
import com.cvesters.notula.topic.TopicService;
import com.cvesters.notula.topic.bdo.TopicAction;

class ChangeServiceTest {

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;

	private static final Origin ORIGIN = new Origin(SESSION.principal());

	private static final TestMeeting MEETING = TestMeeting.SPORER_PROJECT;
	private static final long MEETING_ID = MEETING.getId();
	private static final long REVISION = MEETING.getRevision();
	private static final MeetingScope SCOPE = new MeetingScope(MEETING_ID,
			REVISION);

	private static final long TOPIC_ID = 32L;
	private static final long BLOCK_ID = 61L;
	private static final Long AFTER_ID = 7L;

	private final TestMeetingLock meetingLock = new TestMeetingLock();

	private final MeetingService meetings = mock();
	private final TopicService topics = mock();
	private final BlockService blocks = mock();
	private final TextBlockService texts = mock();

	private final ChangeService changeService = new ChangeService(
			meetingLock.lock(), meetings, topics, blocks, texts);

	@Nested
	class Apply {

		@BeforeEach
		void setup() {
			meetingLock.passThrough(REVISION);
		}

		@Test
		void renameMeeting() {
			changeService.apply(ORIGIN, MEETING_ID, new MeetingChangeDto.Rename(
					new TextEditDto(0, 3, "Renamed")));

			verify(meetings).update(eq(ORIGIN), eq(SCOPE), argThat(action -> {
				final var update = (TextUpdate<?>) action;

				assertThat(update.getPosition()).isEqualTo(0);
				assertThat(update.getLength()).isEqualTo(3);
				assertThat(update.getValue()).isEqualTo("Renamed");
				return true;
			}));
		}

		@Test
		void addTopic() {
			changeService.apply(ORIGIN, MEETING_ID,
					new TopicChangeDto.Add(AFTER_ID, "Blockers"));

			verify(topics).create(eq(ORIGIN), eq(SCOPE), argThat(action -> {
				assertThat(action.getAfterId()).contains(AFTER_ID);
				assertThat(action.getName()).isEqualTo("Blockers");
				return true;
			}));
		}

		@Test
		void moveTopic() {
			changeService.apply(ORIGIN, MEETING_ID,
					new TopicChangeDto.Move(TOPIC_ID, AFTER_ID));

			verify(topics).move(eq(ORIGIN), eq(SCOPE), eq(TOPIC_ID),
					argThat(action -> {
						assertThat(action.getAfterId()).contains(AFTER_ID);
						return true;
					}));
		}

		@Test
		void renameTopic() {
			changeService.apply(ORIGIN, MEETING_ID, new TopicChangeDto.Rename(
					TOPIC_ID, new TextEditDto(1, 2, "Renamed")));

			verify(topics).update(eq(ORIGIN), eq(SCOPE), eq(TOPIC_ID),
					argThat(action -> {
						final var update = (TextUpdate<?>) action;

						assertThat(update.getPosition()).isEqualTo(1);
						assertThat(update.getLength()).isEqualTo(2);
						assertThat(update.getValue()).isEqualTo("Renamed");
						return true;
					}));
		}

		@Test
		void scheduleTopic() {
			changeService.apply(ORIGIN, MEETING_ID,
					new TopicChangeDto.Schedule(TOPIC_ID, 5));

			verify(topics).update(eq(ORIGIN), eq(SCOPE), eq(TOPIC_ID),
					argThat(TopicAction.UpdateDuration.class::isInstance));
		}

		@Test
		void removeTopic() {
			changeService.apply(ORIGIN, MEETING_ID,
					new TopicChangeDto.Remove(TOPIC_ID));

			verify(topics).delete(ORIGIN, SCOPE, TOPIC_ID);
		}

		@Test
		void addBlock() {
			changeService.apply(ORIGIN, MEETING_ID,
					new BlockChangeDto.Add(TOPIC_ID, BlockType.TEXT, AFTER_ID));

			verify(blocks).create(eq(ORIGIN), eq(SCOPE), argThat(action -> {
				assertThat(action.getTopicId()).isEqualTo(TOPIC_ID);
				assertThat(action.getType()).isEqualTo(BlockType.TEXT);
				assertThat(action.getAfterId()).contains(AFTER_ID);
				return true;
			}));
		}

		@Test
		void moveBlock() {
			changeService.apply(ORIGIN, MEETING_ID,
					new BlockChangeDto.Move(BLOCK_ID, AFTER_ID));

			verify(blocks).move(eq(ORIGIN), eq(SCOPE), eq(BLOCK_ID),
					argThat(action -> {
						assertThat(action.getAfterId()).contains(AFTER_ID);
						return true;
					}));
		}

		@Test
		void removeBlock() {
			changeService.apply(ORIGIN, MEETING_ID,
					new BlockChangeDto.Remove(BLOCK_ID));

			verify(blocks).delete(ORIGIN, SCOPE, BLOCK_ID);
		}

		@Test
		void editTextBlock() {
			changeService.apply(ORIGIN, MEETING_ID, new TextBlockChangeDto.Edit(
					BLOCK_ID, new TextEditDto(4, 2, "new")));

			verify(texts).update(eq(ORIGIN), eq(SCOPE), eq(BLOCK_ID),
					argThat(action -> {
						final var splice = (TextUpdate<?>) action;

						assertThat(splice.getPosition()).isEqualTo(4);
						assertThat(splice.getLength()).isEqualTo(2);
						assertThat(splice.getValue()).isEqualTo("new");
						return true;
					}));
		}

		@Test
		void scope() {
			final MeetingScope scope = changeService.apply(ORIGIN, MEETING_ID,
					new TopicChangeDto.Remove(TOPIC_ID));

			assertThat(scope).isEqualTo(SCOPE);
		}

		@Test
		void serialised() {
			meetingLock.withhold();

			changeService.apply(ORIGIN, MEETING_ID,
					new TopicChangeDto.Remove(TOPIC_ID));

			verify(meetingLock.lock()).call(eq(MEETING_ID), any());
			verifyNoInteractions(meetings, topics, blocks, texts);
		}

		@Test
		void originNull() {
			final var change = new TopicChangeDto.Remove(TOPIC_ID);

			assertThatThrownBy(
					() -> changeService.apply(null, MEETING_ID, change))
							.isInstanceOf(NullPointerException.class);
		}

		@Test
		void changeNull() {
			assertThatThrownBy(
					() -> changeService.apply(ORIGIN, MEETING_ID, null))
							.isInstanceOf(NullPointerException.class);
		}
	}
}
