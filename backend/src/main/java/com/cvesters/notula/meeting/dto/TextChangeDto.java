package com.cvesters.notula.meeting.dto;

public sealed interface TextChangeDto permits MeetingChangeDto.Rename,
		MeetingChangeDto.Describe, TopicChangeDto.Rename,
		TopicChangeDto.Describe, TextBlockChangeDto.Edit {

	long base();

	TextEditDto edit();
}
