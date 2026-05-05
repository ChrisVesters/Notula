export type TopicInfo = {
	id: number;
	name: string;
};

export type TopicCreateRequest = {
	name: string;
};

export type TopicUpdateNameAction = {
	action: "UPDATE_NAME";
	position: number;
	length: number;
	value: string;
};
