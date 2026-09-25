package com.cvesters.notula.meeting;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.cvesters.notula.common.domain.Splice;
import com.cvesters.notula.common.exception.InvalidActionException;
import com.cvesters.notula.event.EventStorageGateway;
import com.cvesters.notula.event.bdo.EventInfo;
import com.cvesters.notula.event.bdo.MeetingMutation;
import com.cvesters.notula.event.bdo.Mutation;
import com.cvesters.notula.event.bdo.TextBlockMutation;
import com.cvesters.notula.event.bdo.TopicMutation;
import com.cvesters.notula.meeting.bdo.MeetingScope;
import com.cvesters.notula.meeting.dto.MeetingChangeDto;
import com.cvesters.notula.meeting.dto.TextBlockChangeDto;
import com.cvesters.notula.meeting.dto.TextChangeDto;
import com.cvesters.notula.meeting.dto.TopicChangeDto;

@Service
public class TextHistory {

	private final EventStorageGateway eventStorage;

	public TextHistory(final EventStorageGateway eventStorage) {
		this.eventStorage = eventStorage;
	}

	public Splice rebase(final MeetingScope scope, final TextChangeDto change) {
		Objects.requireNonNull(scope);
		Objects.requireNonNull(change);

		if (change.base() >= scope.revision()) {
			throw new InvalidActionException();
		}

		final List<Splice> priors = eventStorage
				.findAllSince(scope.meetingId(), change.base())
				.stream()
				.map(EventInfo::getMutation)
				.flatMap(mutation -> sameText(change, mutation).stream())
				.toList();

		Splice rebased = change.edit().toBdo();
		for (final Splice prior : priors) {
			rebased = rebased.rebasedOnto(prior);
		}

		return rebased;
	}

	private static Optional<Splice> sameText(final TextChangeDto change,
			final Mutation mutation) {
		return switch (mutation) {
			case MeetingMutation.Rename m
					when change instanceof MeetingChangeDto.Rename ->
					Optional.of(m.edit());
			case MeetingMutation.Describe m
					when change instanceof MeetingChangeDto.Describe ->
					Optional.of(m.edit());
			case TopicMutation.Rename m
					when change instanceof TopicChangeDto.Rename c
							&& c.topic() == m.topicId() ->
					Optional.of(m.edit());
			case TopicMutation.Describe m
					when change instanceof TopicChangeDto.Describe c
							&& c.topic() == m.topicId() ->
					Optional.of(m.edit());
			case TextBlockMutation.Edit m
					when change instanceof TextBlockChangeDto.Edit c
							&& c.block() == m.blockId() ->
					Optional.of(m.edit());
			default -> Optional.empty();
		};
	}
}
