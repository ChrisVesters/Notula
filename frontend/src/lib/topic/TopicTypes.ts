export type TopicCreateRequest = {
	sequenceId: number;
	name: string;
};

export type TopicAction =
	| TopicUpdateNameAction
	| TopicUpdateDescriptionAction
	| TopicUpdateDurationAction;

export type TopicUpdateNameAction = {
	action: "UPDATE_NAME";
	position: number;
	length: number;
	value: string;
};

export type TopicUpdateDescriptionAction = {
	action: "UPDATE_DESCRIPTION";
	position: number;
	length: number;
	value: string;
};

export type TopicUpdateDurationAction = {
	action: "UPDATE_DURATION";
	duration: number | null;
};

export type TopicEvent = {
	target: "TOPIC";
	topicId: number;
	mutation: TopicMutation;
};

export type TopicMutation =
	| TopicMutationCreate
	| TopicMutationUpdateName
	| TopicMutationUpdateDescription
	| TopicMutationUpdateDuration
	| TopicMutationDelete;

export type TopicMutationCreate = {
	action: "CREATE";
	sequenceId: number;
	name: string;
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
