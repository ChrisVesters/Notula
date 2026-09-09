package com.cvesters.notula.meeting.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY,
		property = "type")
@JsonSubTypes({
		@Type(value = MeetingChangeDto.Rename.class, name = "RENAME_MEETING"),
		@Type(value = MeetingChangeDto.Describe.class,
				name = "DESCRIBE_MEETING"),
		@Type(value = TopicChangeDto.Add.class, name = "ADD_TOPIC"),
		@Type(value = TopicChangeDto.Move.class, name = "MOVE_TOPIC"),
		@Type(value = TopicChangeDto.Rename.class, name = "RENAME_TOPIC"),
		@Type(value = TopicChangeDto.Describe.class, name = "DESCRIBE_TOPIC"),
		@Type(value = TopicChangeDto.Schedule.class, name = "SCHEDULE_TOPIC"),
		@Type(value = TopicChangeDto.Remove.class, name = "REMOVE_TOPIC"),
		@Type(value = BlockChangeDto.Add.class, name = "ADD_BLOCK"),
		@Type(value = BlockChangeDto.Move.class, name = "MOVE_BLOCK"),
		@Type(value = BlockChangeDto.Remove.class, name = "REMOVE_BLOCK"),
		@Type(value = TextBlockChangeDto.Edit.class,
				name = "EDIT_TEXT_BLOCK") })
public sealed interface ChangeDto permits MeetingChangeDto, TopicChangeDto,
		BlockChangeDto, TextBlockChangeDto {
}
