import type { Attachment } from "svelte/attachments";

export const DropPosition = {
	BEFORE: "BEFORE",
	AFTER: "AFTER"
} as const;

export type DropPosition = (typeof DropPosition)[keyof typeof DropPosition];

export type DropTarget = {
	id: number;
	position: DropPosition;
};

export type ReorderHandlerOptions = {
	order: () => ReadonlyArray<number>;
	onDragChange: (id: number | null) => void;
	onDropChange: (target: DropTarget | null) => void;
	onMove: (id: number, afterId: number | null) => void;
};

const ITEM = "[data-reorder-id]";
const HANDLE = "[data-reorder-handle]";

export const reorderHandler = (
	options: ReorderHandlerOptions
): Attachment<HTMLElement> => {
	return (list: HTMLElement) => {
		let armed: HTMLElement | null = null;
		let dragged: number | null = null;
		let target: DropTarget | null = null;

		const itemOf = (element: EventTarget | null): HTMLElement | null => {
			if (!(element instanceof Element)) {
				return null;
			}

			const item = element.closest<HTMLElement>(ITEM);

			return item?.parentElement === list ? item : null;
		};

		const idOf = (item: HTMLElement) => Number(item.dataset.reorderId);

		const setDragged = (value: number | null) => {
			dragged = value;
			options.onDragChange(value);
		};

		const setTarget = (value: DropTarget | null) => {
			if (
				target?.id === value?.id &&
				target?.position === value?.position
			) {
				return;
			}

			target = value;
			options.onDropChange(value);
		};

		const disarm = () => {
			if (armed) {
				armed.draggable = false;
				armed = null;
			}
		};

		const handlePointerDown = (event: PointerEvent) => {
			disarm();

			// The item is only draggable while its handle is held, so that any
			// text it contains remains selectable.
			const handle =
				event.target instanceof Element
					? event.target.closest(HANDLE)
					: null;
			const item = itemOf(handle);

			if (item && item === itemOf(event.target)) {
				item.draggable = true;
				armed = item;
			}
		};

		const handleDragStart = (event: DragEvent) => {
			const item = itemOf(event.target);

			if (!event.dataTransfer || !item || item !== event.target) {
				return;
			}

			event.dataTransfer.effectAllowed = "move";
			// Firefox only starts a drag once data has been set.
			event.dataTransfer.setData("text/plain", "");

			setDragged(idOf(item));
		};

		const handleDragEnd = () => {
			disarm();
			setDragged(null);
			setTarget(null);
		};

		const handleDragOver = (event: DragEvent) => {
			const item = itemOf(event.target);

			if (!event.dataTransfer || dragged === null || !item) {
				return;
			}

			event.preventDefault();
			event.dataTransfer.dropEffect = "move";

			const bounds = item.getBoundingClientRect();
			const middle = bounds.top + bounds.height / 2;

			setTarget({
				id: idOf(item),
				position:
					event.clientY < middle
						? DropPosition.BEFORE
						: DropPosition.AFTER
			});
		};

		const handleDragLeave = (event: DragEvent) => {
			if (itemOf(event.relatedTarget) === null) {
				setTarget(null);
			}
		};

		const handleDrop = (event: DragEvent) => {
			const id = dragged;
			const at = target;

			setTarget(null);

			if (id === null || at === null) {
				return;
			}

			event.preventDefault();

			const order = options.order();
			const previousOf = (other: number) =>
				order[order.indexOf(other) - 1] ?? null;

			const afterId =
				at.position === DropPosition.BEFORE ? previousOf(at.id) : at.id;

			if (afterId === id || afterId === previousOf(id)) {
				return;
			}

			options.onMove(id, afterId);
		};

		list.addEventListener("pointerdown", handlePointerDown);
		list.addEventListener("pointerup", disarm);
		list.addEventListener("dragstart", handleDragStart);
		list.addEventListener("dragend", handleDragEnd);
		list.addEventListener("dragover", handleDragOver);
		list.addEventListener("dragleave", handleDragLeave);
		list.addEventListener("drop", handleDrop);

		return () => {
			list.removeEventListener("pointerdown", handlePointerDown);
			list.removeEventListener("pointerup", disarm);
			list.removeEventListener("dragstart", handleDragStart);
			list.removeEventListener("dragend", handleDragEnd);
			list.removeEventListener("dragover", handleDragOver);
			list.removeEventListener("dragleave", handleDragLeave);
			list.removeEventListener("drop", handleDrop);
		};
	};
};
