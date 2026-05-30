import { BlockType } from "$lib/block/BlockTypes";

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
	sequenceId: number;
} & BlockContent;

export type BlockContent = TextBlockContent;

export type TextBlockContent = {
	type: typeof BlockType.TEXT;
	content: string;
};

