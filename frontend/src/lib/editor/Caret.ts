import { tick } from "svelte";

import { moved } from "./TextEdit";

type Element = HTMLInputElement | HTMLTextAreaElement;

type Selection = {
	start: number;
	end: number;
};

// A change from someone else arrives as a new value, which Svelte writes to
// the element, collapsing the caret to the end. Called from $effect.pre the
// element still holds the previous text, so text the user typed locally is
// recognised by the element already matching, and anything else is a change
// to carry the caret across.
export function keepCaret(
	element: Element | undefined,
	next: string,
	afterApplied?: () => void
): void {
	if (!element || element.value === next) {
		return;
	}

	const selection = selectionAfter(element, next);

	tick().then(() => {
		if (selection) {
			element.setSelectionRange(selection.start, selection.end);
		}

		afterApplied?.();
	});
}

function selectionAfter(element: Element, next: string): Selection | null {
	if (document.activeElement !== element) {
		return null;
	}

	const previous = element.value;

	return {
		start: moved(previous, next, element.selectionStart ?? 0),
		end: moved(previous, next, element.selectionEnd ?? 0)
	};
}
