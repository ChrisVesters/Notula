export const BlockType = {
	TEXT: "TEXT"
} as const;

export type BlockType = (typeof BlockType)[keyof typeof BlockType];

export type BlockInfo = {
	id: number;
	topicId: number;
	type: BlockType;
	sequenceId: number;
};

export type BlockCreateAction = {
	topicId: number;
	type: BlockType;
	sequenceId: number;
};

export type BlockMoveAction = {
	blockId: number;
	sequenceId: number;
};

export type BlockDeleteAction = {
	blockId: number;
};

export type BlockEvent = {
	target: "BLOCK";
	blockId: number;
	mutation: BlockMutation;
};

export type BlockMutation =
	| BlockMutationCreate
	| BlockMutationMove
	| BlockMutationDelete;

export type BlockMutationCreate = {
	action: "CREATE";
	topicId: number;
	type: BlockType;
	sequenceId: number;
};

export type BlockMutationMove = {
	action: "MOVE";
	sequenceId: number;
};

export type BlockMutationDelete = {
	action: "DELETE";
};
