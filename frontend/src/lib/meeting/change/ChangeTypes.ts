import type { BlockType } from "$lib/block/BlockTypes";

export type Change =
	| MeetingChange
	| TopicChange
	| BlockChange
	| TextBlockChange;

export type MeetingChange =
	| ({ type: "RENAME_MEETING" } & TextEdit)
	| ({ type: "DESCRIBE_MEETING" } & TextEdit);

export type TopicChange =
	| { type: "ADD_TOPIC"; sequenceId: number; name: string }
	| { type: "MOVE_TOPIC"; topic: number; sequenceId: number }
	| ({ type: "RENAME_TOPIC"; topic: number } & TextEdit)
	| ({ type: "DESCRIBE_TOPIC"; topic: number } & TextEdit)
	| { type: "SCHEDULE_TOPIC"; topic: number; minutes: number | null }
	| { type: "REMOVE_TOPIC"; topic: number };

export type BlockChange =
	| {
			type: "ADD_BLOCK";
			topic: number;
			blockType: BlockType;
			sequenceId: number;
	  }
	| { type: "MOVE_BLOCK"; block: number; sequenceId: number }
	| { type: "REMOVE_BLOCK"; block: number };

export type TextBlockChange = {
	type: "EDIT_TEXT_BLOCK";
	block: number;
} & TextEdit;

export type TextEdit = {
	position: number;
	length: number;
	value: string;
};

export type Submission = {
	id: string;
	change: Change;
};

export type Rejected = {
	id: string;
	retryable: boolean;
	reason: string;
};

export type { BlockType };
