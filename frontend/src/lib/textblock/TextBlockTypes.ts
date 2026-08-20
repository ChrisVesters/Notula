
export type TextBlockUpdateAction = TextBlockUpdateContentAction;

export type TextBlockUpdateContentAction = {
	meetingId: number;
	topicId: number;
	blockId: number;
	action: "UPDATE_CONTENT";
	position: number;
	length: number;
	value: string;
};

export type TextBlockEvent = {
	target: "TEXT_BLOCK";
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
