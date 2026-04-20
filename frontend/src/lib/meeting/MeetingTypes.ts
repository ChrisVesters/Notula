import type { TopicInfo } from "$lib/topic/TopicTypes";

export type MeetingInfo = {
	id: number;
	name: string;
};

export type MeetingDetails = {
	info: MeetingInfo;
	topics: Array<TopicInfo>;
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
export type MeetingActionResponse = MeetingAddTopicResponse;

export type MeetingAddTopicResponse = {
	type: "TOPIC_CREATE";
	id: number;
	name: string;
};
