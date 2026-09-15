package com.cvesters.notula.meeting.dto;

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
}
