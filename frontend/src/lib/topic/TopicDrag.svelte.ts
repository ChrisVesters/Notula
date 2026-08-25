export type DropPosition = "BEFORE" | "AFTER";

export type DragSource = {
	meetingId: number;
	topicId: number;
	sequenceId: number;
};

export type DragTarget = {
	topicId: number;
	position: DropPosition;
};

let source: DragSource | null = $state(null);
let target: DragTarget | null = $state(null);

export const topicDrag = {
	get source() {
		return source;
	},
	get target() {
		return target;
	},
	start: (value: DragSource) => {
		source = value;
		target = null;
	},
	over: (value: DragTarget) => {
		target = value;
	},
	clear: () => {
		target = null;
	},
	end: () => {
		source = null;
		target = null;
	}
};
