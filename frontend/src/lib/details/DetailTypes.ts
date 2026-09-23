import { BlockType } from "$lib/block/BlockTypes";

export type MeetingDetails = {
	id: number;
	name: string;
	description: string;
	revision: number;

	topics: Array<TopicDetails>;
};

export type TopicDetails = {
	id: number;
	rank: string;
	name: string;
	description: string;
	duration: number | null;

	blocks: Array<BlockDetails>;
};

export type BlockDetails = {
	id: number;
	rank: string;
} & BlockContent;

export type BlockContent = TextBlockContent;

export type TextBlockContent = {
	type: typeof BlockType.TEXT;
	content: string;
};
