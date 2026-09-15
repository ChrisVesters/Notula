import type { TextEdit } from "$lib/meeting/change/ChangeTypes";

type Splice = {
	position: number;
	removed: number;
	inserted: number;
};

export function applied(text: string, edit: TextEdit): string {
	const before = text.slice(0, edit.position);
	const after = text.slice(edit.position + edit.length);

	return before + edit.value + after;
}

export function moved(previous: string, next: string, index: number): number {
	const { position, removed, inserted } = spliced(previous, next);

	if (index <= position) {
		return index;
	}

	if (index >= position + removed) {
		return index + inserted - removed;
	}

	return position + inserted;
}

function spliced(previous: string, next: string): Splice {
	const shortest = Math.min(previous.length, next.length);

	let start = 0;
	while (start < shortest && previous[start] === next[start]) {
		start++;
	}

	let tail = 0;
	while (
		tail < shortest - start &&
		previous[previous.length - 1 - tail] === next[next.length - 1 - tail]
	) {
		tail++;
	}

	return {
		position: start,
		removed: previous.length - start - tail,
		inserted: next.length - start - tail
	};
}
