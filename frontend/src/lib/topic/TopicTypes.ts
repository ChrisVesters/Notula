import type { EventOrigin } from "$lib/common/EventTypes";

export type TopicCreateAction = {
	meetingId: number;
	sequenceId: number;
	name: string;
};

export type TopicMoveAction = {
	topicId: number;
	sequenceId: number;
};

export type TopicUpdateAction =
	| TopicUpdateNameAction
	| TopicUpdateDescriptionAction
	| TopicUpdateDurationAction;

export type TopicUpdateNameAction = {
	topicId: number;
	action: "UPDATE_NAME";
	position: number;
	length: number;
	value: string;
};

export type TopicUpdateDescriptionAction = {
	topicId: number;
	action: "UPDATE_DESCRIPTION";
	position: number;
	length: number;
	value: string;
};

export type TopicUpdateDurationAction = {
	topicId: number;
	action: "UPDATE_DURATION";
	duration: number | null;
};

export type TopicDeleteAction = {
	topicId: number;
};

export type TopicEvent = {
	target: "TOPIC";
	topicId: number;
	mutation: TopicMutation;
	origin: EventOrigin;
};

export type TopicMutation =
	| TopicMutationCreate
	| TopicMutationMove
	| TopicMutationUpdateName
	| TopicMutationUpdateDescription
	| TopicMutationUpdateDuration
	| TopicMutationDelete;

export type TopicMutationCreate = {
	action: "CREATE";
	meetingId: number;
	sequenceId: number;
	name: string;
};

export type TopicMutationMove = {
	action: "MOVE";
	sequenceId: number;
};

export type TopicMutationUpdateName = {
	action: "UPDATE_NAME";
	position: number;
	length: number;
	value: string;
};

export type TopicMutationUpdateDescription = {
	action: "UPDATE_DESCRIPTION";
	position: number;
	length: number;
	value: string;
};

export type TopicMutationUpdateDuration = {
	action: "UPDATE_DURATION";
	duration: number | null;
};

export type TopicMutationDelete = {
	action: "DELETE";
};
