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

// The server puts the edit it applied earlier first at a tie, so a client
// rebasing a remote edit over its own pending one passes `first` to arrive at
// the same text.
export function rebased(
	edit: TextEdit,
	prior: TextEdit,
	first: boolean = false
): TextEdit {
	const end = edit.position + edit.length;
	const priorEnd = prior.position + prior.length;
	const leads =
		edit.position < prior.position ||
		(first && edit.position === prior.position);

	const start = startAfter(edit.position, prior, leads);
	const stop = Math.max(start, stopAfter(end, prior));

	// Whatever the prior edit inserted inside the range this one replaces was
	// typed by someone, so it is put back rather than deleted with it.
	const covers = leads && end > priorEnd;

	return {
		position: start,
		length: stop - start,
		value: covers ? edit.value + prior.value : edit.value
	};
}

function startAfter(index: number, prior: TextEdit, leads: boolean): number {
	if (index < prior.position || (leads && index === prior.position)) {
		return index;
	}

	if (index <= prior.position + prior.length) {
		return prior.position + prior.value.length;
	}

	return index - prior.length + prior.value.length;
}

function stopAfter(index: number, prior: TextEdit): number {
	if (index <= prior.position) {
		return index;
	}

	if (index <= prior.position + prior.length) {
		return prior.position;
	}

	return index - prior.length + prior.value.length;
}

// One edit that does what applying `first` and then `second` does, or null
// when `second` does not touch what `first` produced: two edits apart would
// need the unchanged text between them, which an edit does not carry.
export function composed(first: TextEdit, second: TextEdit): TextEdit | null {
	const start = first.position;
	const end = first.position + first.value.length;
	const secondEnd = second.position + second.length;
	if (second.position > end || secondEnd < start) {
		return null;
	}

	const before = Math.max(0, start - second.position);
	const after = Math.max(0, secondEnd - end);
	const kept = first.value.slice(0, Math.max(0, second.position - start));
	const rest = first.value.slice(
		Math.min(first.value.length, secondEnd - start)
	);

	return {
		position: Math.min(start, second.position),
		length: before + first.length + after,
		value: kept + second.value + rest
	};
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
