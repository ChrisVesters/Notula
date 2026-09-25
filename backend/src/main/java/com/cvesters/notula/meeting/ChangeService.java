package com.cvesters.notula.meeting;

import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.cvesters.notula.block.BlockService;
import com.cvesters.notula.common.domain.ChangeId;
import com.cvesters.notula.common.domain.Origin;
import com.cvesters.notula.event.EventStorageGateway;
import com.cvesters.notula.meeting.bdo.MeetingScope;
import com.cvesters.notula.meeting.dto.BlockChangeDto;
import com.cvesters.notula.meeting.dto.ChangeDto;
import com.cvesters.notula.meeting.dto.MeetingChangeDto;
import com.cvesters.notula.meeting.dto.TextBlockChangeDto;
import com.cvesters.notula.meeting.dto.TopicChangeDto;
import com.cvesters.notula.textblock.TextBlockService;
import com.cvesters.notula.topic.TopicService;

@Service
public class ChangeService {

	private final MeetingLock meetingLock;
	private final TextHistory history;
	private final EventStorageGateway eventStorage;

	private final MeetingService meetings;
	private final TopicService topics;
	private final BlockService blocks;
	private final TextBlockService texts;

	public ChangeService(final MeetingLock meetingLock,
			final TextHistory history, final EventStorageGateway eventStorage,
			final MeetingService meetings, final TopicService topics,
			final BlockService blocks, final TextBlockService texts) {
		this.meetingLock = meetingLock;
		this.history = history;
		this.eventStorage = eventStorage;
		this.meetings = meetings;
		this.topics = topics;
		this.blocks = blocks;
		this.texts = texts;
	}

	public MeetingScope apply(final Origin origin, final ChangeId changeId,
			final long meetingId, final ChangeDto change) {
		Objects.requireNonNull(origin);
		Objects.requireNonNull(changeId);
		Objects.requireNonNull(change);

		final UUID id = changeId.value();
		if (id == null) {
			return commit(origin, meetingId, null, change);
		}

		// A client resends the change it had in flight after a reconnect, not
		// knowing whether it committed. Looking it up has to happen before the
		// revision is bumped: a revision spent on a change that is not applied
		// publishes nothing, and every other client would see a gap.
		return meetingLock.hold(meetingId,
				() -> eventStorage.findByChangeId(meetingId, id)
						.map(logged -> new MeetingScope(meetingId,
								logged.getRevision(), id))
						.orElseGet(() -> commit(origin, meetingId, id, change)));
	}

	private MeetingScope commit(final Origin origin, final long meetingId,
			final UUID changeId, final ChangeDto change) {
		return meetingLock.call(meetingId, bumped -> {
			final var scope = new MeetingScope(meetingId, bumped.revision(),
					changeId);
			dispatch(origin, scope, change);

			return scope;
		});
	}

	private void dispatch(final Origin origin, final MeetingScope scope,
			final ChangeDto change) {
		switch (change) {
			case MeetingChangeDto.Rename c -> meetings.update(origin, scope,
					c.toBdo(history.rebase(scope, c)));
			case MeetingChangeDto.Describe c -> meetings.update(origin, scope,
					c.toBdo(history.rebase(scope, c)));

			case TopicChangeDto.Add c -> topics.create(origin, scope,
					c.toBdo());
			case TopicChangeDto.Move c -> topics.move(origin, scope,
					c.topic(), c.toBdo());
			case TopicChangeDto.Rename c -> topics.update(origin, scope,
					c.topic(), c.toBdo(history.rebase(scope, c)));
			case TopicChangeDto.Describe c -> topics.update(origin, scope,
					c.topic(), c.toBdo(history.rebase(scope, c)));
			case TopicChangeDto.Schedule c -> topics.update(origin, scope,
					c.topic(), c.toBdo());
			case TopicChangeDto.Remove c -> topics.delete(origin, scope,
					c.topic());

			case BlockChangeDto.Add c -> blocks.create(origin, scope,
					c.toBdo());
			case BlockChangeDto.Move c -> blocks.move(origin, scope,
					c.block(), c.toBdo());
			case BlockChangeDto.Remove c -> blocks.delete(origin, scope,
					c.block());

			case TextBlockChangeDto.Edit c -> texts.update(origin, scope,
					c.block(), c.toBdo(history.rebase(scope, c)));
		}
	}
}
