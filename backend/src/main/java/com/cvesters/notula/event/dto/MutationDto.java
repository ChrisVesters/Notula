package com.cvesters.notula.event.dto;

import java.util.Objects;

import com.cvesters.notula.event.bdo.BlockMutation;
import com.cvesters.notula.event.bdo.MeetingMutation;
import com.cvesters.notula.event.bdo.Mutation;
import com.cvesters.notula.event.bdo.TextBlockMutation;
import com.cvesters.notula.event.bdo.TopicMutation;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY,
		property = "type")
@JsonSubTypes({
		@Type(value = MeetingMutationDto.Add.class, name = "ADD_MEETING"),
		@Type(value = MeetingMutationDto.Rename.class, name = "RENAME_MEETING"),
		@Type(value = MeetingMutationDto.Describe.class,
				name = "DESCRIBE_MEETING"),
		@Type(value = MeetingMutationDto.Remove.class, name = "REMOVE_MEETING"),
		@Type(value = TopicMutationDto.Add.class, name = "ADD_TOPIC"),
		@Type(value = TopicMutationDto.Move.class, name = "MOVE_TOPIC"),
		@Type(value = TopicMutationDto.Rename.class, name = "RENAME_TOPIC"),
		@Type(value = TopicMutationDto.Describe.class, name = "DESCRIBE_TOPIC"),
		@Type(value = TopicMutationDto.Schedule.class, name = "SCHEDULE_TOPIC"),
		@Type(value = TopicMutationDto.Remove.class, name = "REMOVE_TOPIC"),
		@Type(value = BlockMutationDto.Add.class, name = "ADD_BLOCK"),
		@Type(value = BlockMutationDto.Move.class, name = "MOVE_BLOCK"),
		@Type(value = BlockMutationDto.Remove.class, name = "REMOVE_BLOCK"),
		@Type(value = TextBlockMutationDto.Edit.class,
				name = "EDIT_TEXT_BLOCK") })
public sealed interface MutationDto permits MeetingMutationDto,
		TopicMutationDto, BlockMutationDto, TextBlockMutationDto {

	static MutationDto of(final Mutation mutation) {
		Objects.requireNonNull(mutation);

		return switch (mutation) {
			case MeetingMutation m -> MeetingMutationDto.of(m);
			case TopicMutation m -> TopicMutationDto.of(m);
			case BlockMutation m -> BlockMutationDto.of(m);
			case TextBlockMutation m -> TextBlockMutationDto.of(m);
		};
	}

	Mutation toBdo();
}
