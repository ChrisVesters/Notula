import type { Attachment } from "svelte/attachments";

export type FocusHandlerOptions = {
	onFocusChange: (focused: boolean) => void;
};

type Element = HTMLInputElement | HTMLTextAreaElement;

export const focusHandler = (
	options: FocusHandlerOptions
): Attachment<Element> => {
	return (node: Element) => {
		const handleFocus = () => {
			options.onFocusChange(true);
		};

		const handleBlur = () => {
			options.onFocusChange(false);
		};

		node.addEventListener("focus", handleFocus);
		node.addEventListener("blur", handleBlur);

		return () => {
			node.removeEventListener("focus", handleFocus);
			node.removeEventListener("blur", handleBlur);
		};
	};
};
