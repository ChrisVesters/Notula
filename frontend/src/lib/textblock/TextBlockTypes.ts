import type { EventOrigin } from "$lib/common/EventTypes";

export type TextBlockEvent = {
	target: "TEXT_BLOCK";
	block: number;
	mutation: TextBlockMutation;
	origin: EventOrigin;
};

export type TextBlockMutation = TextBlockMutationUpdateContent;

export type TextBlockMutationUpdateContent = {
	action: "UPDATE_CONTENT";
	position: number;
	length: number;
	value: string;
};
