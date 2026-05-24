export const BlockType = {
    TEXT: "TEXT"
} as const;

export type BlockType = typeof BlockType[keyof typeof BlockType];

export type BlockInfo = {
	id: number;
	topicId: number;
	type: BlockType;
	sequenceId: number;
};

export type BlockCreateRequest = {
	type: BlockType;
	sequenceId: number;
};


export type BlockEvent = {
	target: "BLOCK";
	topicId: number;
	blockId: number;
	mutation: BlockMutation;
};

export type BlockMutation = {
	action: "CREATE";
	type: BlockType;
	sequenceId: number;
};
