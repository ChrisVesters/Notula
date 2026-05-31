import type { BlockEvent } from "$lib/block/BlockTypes";
import type { TopicEvent } from "$lib/topic/TopicTypes";

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

export type MeetingUpdateDescriptionAction = {
	action: "UPDATE_DESCRIPTION";
	position: number;
	length: number;
	value: string;
};

// TODO: move somewhere else?
export type MeetingMessage = MeetingEvent | TopicEvent | BlockEvent;

export type MeetingEvent = {
	target: "MEETING";
	meetingId: number;
	mutation: MeetingMutation;
};

export type MeetingMutation =
	| MeetingMutationCreate
	| MeetingMutationUpdateName
	| MeetingMutationUpdateDescription;

export type MeetingMutationCreate = {
	action: "CREATE";
	name: string;
};

export type MeetingMutationUpdateName = {
	action: "UPDATE_NAME";
	position: number;
	length: number;
	value: string;
};

export type MeetingMutationUpdateDescription = {
	action: "UPDATE_DESCRIPTION";
	position: number;
	length: number;
	value: string;
};
