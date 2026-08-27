import type { Attachment } from "svelte/attachments";

export const DropPosition = {
	BEFORE: "BEFORE",
	AFTER: "AFTER"
} as const;

export type DropPosition = (typeof DropPosition)[keyof typeof DropPosition];

export type ReorderHandlerOptions = {
	sequenceId: number;
	onDragChange: (dragged: boolean) => void;
	onDropChange: (position: DropPosition | null) => void;
	onMove: (sequenceId: number) => void;
};

type DragSource = {
	list: Element;
	sequenceId: number;
	move: (sequenceId: number) => void;
};

const HANDLE = "[data-reorder-handle]";

/** Only a single item can be dragged at a time. */
let source: DragSource | null = null;

/**
 * Makes an item of a sorted list re-orderable by dragging it onto one of its
 * siblings. Items can only be dropped onto siblings within the same list.
 *
 * The drag is started from the descendant marked with `data-reorder-handle`.
 */
// TODO: review and see if you can clean up this logic
export const reorderHandler = (
	options: ReorderHandlerOptions
): Attachment<HTMLElement> => {
	return (node: HTMLElement) => {
		let position: DropPosition | null = null;

		const setPosition = (value: DropPosition | null) => {
			if (position === value) {
				return;
			}

			position = value;
			options.onDropChange(value);
		};

		const handlePointerDown = (event: PointerEvent) => {
			// The item is only draggable while its handle is held, so that any
			// text it contains remains selectable.
			const target = event.target;

			node.draggable =
				target instanceof Element && target.closest(HANDLE) !== null;
		};

		const handlePointerUp = () => {
			node.draggable = false;
		};

		const handleDragStart = (event: DragEvent) => {
			if (!event.dataTransfer || !node.parentElement) {
				return;
			}

			event.dataTransfer.effectAllowed = "move";
			// Firefox only starts a drag once data has been set.
			event.dataTransfer.setData("text/plain", "");

			source = {
				list: node.parentElement,
				sequenceId: options.sequenceId,
				move: options.onMove
			};

			options.onDragChange(true);
		};

		const handleDragEnd = () => {
			node.draggable = false;
			source = null;

			options.onDragChange(false);
			setPosition(null);
		};

		const handleDragOver = (event: DragEvent) => {
			if (!event.dataTransfer || source?.list !== node.parentElement) {
				return;
			}

			event.preventDefault();
			event.dataTransfer.dropEffect = "move";

			const bounds = node.getBoundingClientRect();
			const middle = bounds.top + bounds.height / 2;

			setPosition(
				event.clientY < middle
					? DropPosition.BEFORE
					: DropPosition.AFTER
			);
		};

		const handleDragLeave = (event: DragEvent) => {
			// Moving onto one of its own descendants is not leaving the item.
			const target = event.relatedTarget;

			if (target instanceof Node && node.contains(target)) {
				return;
			}

			setPosition(null);
		};

		const handleDrop = (event: DragEvent) => {
			const dropped = source;
			const droppedAt = position;

			setPosition(null);

			if (!dropped || droppedAt === null) {
				return;
			}

			event.preventDefault();

			// The index of the gap the item is dropped in, before removal.
			const gap =
				droppedAt === DropPosition.BEFORE
					? options.sequenceId
					: options.sequenceId + 1;
			const sequenceId = gap > dropped.sequenceId ? gap - 1 : gap;

			if (sequenceId === dropped.sequenceId) {
				return;
			}

			dropped.move(sequenceId);
		};

		node.addEventListener("pointerdown", handlePointerDown);
		node.addEventListener("pointerup", handlePointerUp);
		node.addEventListener("dragstart", handleDragStart);
		node.addEventListener("dragend", handleDragEnd);
		node.addEventListener("dragover", handleDragOver);
		node.addEventListener("dragleave", handleDragLeave);
		node.addEventListener("drop", handleDrop);

		return () => {
			node.removeEventListener("pointerdown", handlePointerDown);
			node.removeEventListener("pointerup", handlePointerUp);
			node.removeEventListener("dragstart", handleDragStart);
			node.removeEventListener("dragend", handleDragEnd);
			node.removeEventListener("dragover", handleDragOver);
			node.removeEventListener("dragleave", handleDragLeave);
			node.removeEventListener("drop", handleDrop);
		};
	};
};
