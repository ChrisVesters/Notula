import type { BlockEvent } from "$lib/block/BlockTypes";
import type { TopicEvent } from "$lib/topic/TopicTypes";

export type MeetingInfo = {
	id: number;
	name: string;
};

export type MeetingCreateAction = {
	name: string;
};

export type MeetingUpdateAction =
	| MeetingUpdateNameAction
	| MeetingUpdateDescriptionAction;

export type MeetingUpdateNameAction = {
	meetingId: number;
	action: "UPDATE_NAME";
	position: number;
	length: number;
	value: string;
};

export type MeetingUpdateDescriptionAction = {
	meetingId: number;
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
	| MeetingMutationUpdateDescription
	| MeetingMutationDelete;

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

export type MeetingMutationDelete = {
	action: "DELETE";
};
