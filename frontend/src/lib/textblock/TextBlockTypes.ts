export type TextBlockUpdateContentAction = {
	action: "UPDATE_CONTENT";
	position: number;
	length: number;
	value: string;
};

export type TextBlockEvent = {
	target: "TEXT_BLOCK";
	topicId: number;
	block: number;
	mutation: TextBlockMutation;
};

export type TextBlockMutation = TextBlockMutationUpdateContent;

export type TextBlockMutationUpdateContent = {
	action: "UPDATE_CONTENT";
	position: number;
	length: number;
	value: string;
};
