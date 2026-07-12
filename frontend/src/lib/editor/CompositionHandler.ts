import type { Attachment } from "svelte/attachments";
import type { UpdateAction } from "./ActionTypes";

export type CompositionHandlerOptions = {
	onCommit: (action: UpdateAction) => void;
};

type Element = HTMLTextAreaElement | HTMLInputElement;

export const compositionHandler = (
	options: CompositionHandlerOptions
): Attachment<Element> => {
	return (node: Element) => {
		let start = 0;
		let end = 0;

		const handleCompositionStart = () => {
			start = node.selectionStart ?? 0;
			end = node.selectionEnd ?? start;
		};

		const handleCompositionEnd = (event: Event) => {
			const e = event as CompositionEvent;
			const value = e.data ?? "";

			options.onCommit({
				position: start,
				length: end - start,
				value
			});
		};

		node.addEventListener("compositionstart", handleCompositionStart);
		node.addEventListener("compositionend", handleCompositionEnd);

		return () => {
			node.removeEventListener(
				"compositionstart",
				handleCompositionStart
			);

			node.removeEventListener("compositionend", handleCompositionEnd);
		};
	};
};
