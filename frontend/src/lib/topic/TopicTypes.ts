export type TopicCreateRequest = {
	name: string;
};

export type TopicUpdateNameAction = {
	action: "UPDATE_NAME";
	position: number;
	length: number;
	value: string;
};

export type TopicEvent = {
	target: "TOPIC";
	topicId: number;
	mutation: TopicMutation;
};

export type TopicMutation = TopicMutationCreate | TopicMutationUpdateName;

export type TopicMutationCreate = {
	action: "CREATE";
	name: string;
};

export type TopicMutationUpdateName = {
	action: "UPDATE_NAME";
	position: number;
	length: number;
	value: string;
};
