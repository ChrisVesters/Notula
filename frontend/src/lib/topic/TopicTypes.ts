export type TopicCreateRequest = {
	name: string;
};

export type TopicAction = TopicUpdateNameAction | TopicUpdateDescriptionAction;

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

export type TopicEvent = {
	target: "TOPIC";
	topicId: number;
	mutation: TopicMutation;
};

export type TopicMutation =
	| TopicMutationCreate
	| TopicMutationUpdateName
	| TopicMutationUpdateDescription
	| TopicMutationDelete;

export type TopicMutationCreate = {
	action: "CREATE";
	name: string;
	description: string;
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

export type TopicMutationDelete = {
	action: "DELETE";
};
