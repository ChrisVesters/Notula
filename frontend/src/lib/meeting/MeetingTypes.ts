import type { BlockEvent } from "$lib/block/BlockTypes";

export type MeetingInfo = {
	id: number;
	name: string;
};

export type MeetingCreateRequest = {
	name: string;
};

// TODO: classes?
export type MeetingUpdateNameAction = {
	action: "UPDATE_NAME";
	position: number;
	length: number;
	value: string;
};

// TODO: proper union?
// Or class? because of the type field!
// TODO: event instead of response?
export type MeetingActionResponse = MeetingAddTopicResponse | BlockEvent;

export type MeetingAddTopicResponse = {
	type: "TOPIC_CREATE";
	topicId: number;
	name: string;
};
