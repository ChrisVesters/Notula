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
