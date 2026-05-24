import type { BlockType } from "$lib/block/BlockTypes";

export type MeetingDetails = {
	id: number;
	name: string;

	topics: Array<TopicDetails>;
};

export type TopicDetails = {
	id: number;
	name: string;

	blocks: Array<BlockDetails>;
};

export type BlockDetails = {
	id: number;
	type: BlockType;
	sequenceId: number;
};
