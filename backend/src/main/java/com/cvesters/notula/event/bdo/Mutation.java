package com.cvesters.notula.event.bdo;

public sealed interface Mutation permits MeetingMutation, TopicMutation,
		BlockMutation, TextBlockMutation {
}
