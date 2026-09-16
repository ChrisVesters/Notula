import type { BlockType } from "$lib/block/BlockTypes";
import type { EventOrigin } from "$lib/common/EventTypes";

import type { TextEdit } from "../change/ChangeTypes";

export type MeetingEvent = {
	origin: EventOrigin;
	revision: number;
	mutation: Mutation;
};

export type Mutation =
	| MeetingMutation
	| TopicMutation
	| BlockMutation
	| TextBlockMutation;

export type MeetingMutation =
	| { type: "ADD_MEETING"; name: string }
	| ({ type: "RENAME_MEETING" } & TextEdit)
	| ({ type: "DESCRIBE_MEETING" } & TextEdit)
	| { type: "REMOVE_MEETING" };

export type TopicMutation =
	| { type: "ADD_TOPIC"; topic: number; sequenceId: number; name: string }
	| { type: "MOVE_TOPIC"; topic: number; sequenceId: number }
	| ({ type: "RENAME_TOPIC"; topic: number } & TextEdit)
	| ({ type: "DESCRIBE_TOPIC"; topic: number } & TextEdit)
	| { type: "SCHEDULE_TOPIC"; topic: number; minutes: number | null }
	| { type: "REMOVE_TOPIC"; topic: number };

export type BlockMutation =
	| {
			type: "ADD_BLOCK";
			block: number;
			topic: number;
			blockType: BlockType;
			sequenceId: number;
	  }
	| { type: "MOVE_BLOCK"; block: number; sequenceId: number }
	| { type: "REMOVE_BLOCK"; block: number };

export type TextBlockMutation = {
	type: "EDIT_TEXT_BLOCK";
	block: number;
} & TextEdit;
